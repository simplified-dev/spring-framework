package dev.sbs.serverapi.error;

import dev.sbs.api.client.exception.ApiException;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.serverapi.exception.ServerException;
import dev.sbs.serverapi.version.VersionRegistryService;
import dev.sbs.serverapi.version.exception.InvalidVersionException;
import dev.sbs.serverapi.version.exception.MissingVersionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global exception handler producing consistent error responses for all errors.
 *
 * <p>Performs content negotiation via the {@code Accept} header: browsers receiving
 * {@code text/html} get a Cloudflare-style HTML error page rendered by
 * {@link ErrorPageRenderer}, while API clients get JSON error responses.</p>
 */
@RequiredArgsConstructor
@RestControllerAdvice
public final class ErrorController extends ResponseEntityExceptionHandler {

    private static final @NotNull Pattern VERSION_PREFIX = Pattern.compile("^/v(\\d+)(/.*)$");
    private static final @NotNull String CLOUDFLARE_RAY_HEADER = "Cf-Ray";

    private final @NotNull VersionRegistryService versionRegistryService;

    @Override
    protected @NotNull ResponseEntity<Object> handleExceptionInternal(
            @NotNull Exception ex,
            Object body,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode statusCode,
            @NotNull WebRequest request) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        int code = statusCode.value();
        String reason = ex.getMessage() != null ? ex.getMessage() : HttpStatus.valueOf(code).getReasonPhrase();

        if (acceptsHtml(servletRequest)) {
            HttpHeaders htmlHeaders = new HttpHeaders(headers);
            htmlHeaders.setContentType(MediaType.TEXT_HTML);
            String html = html(code, HttpStatus.valueOf(code).getReasonPhrase(), reason, servletRequest);
            return new ResponseEntity<>(html, htmlHeaders, statusCode);
        }

        return new ResponseEntity<>(buildErrorBody(statusCode, ex.getMessage(), request), headers, statusCode);
    }

    @Override
    protected @NotNull ResponseEntity<Object> handleNoResourceFoundException(
            @NotNull NoResourceFoundException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        Matcher versionMatcher = VERSION_PREFIX.matcher(requestUri);

        if (versionMatcher.matches()) {
            int requestedVersion = Integer.parseInt(versionMatcher.group(1));
            String basePath = versionMatcher.group(2);
            ConcurrentSet<Integer> available = versionRegistryService.getVersionsForPath(basePath);

            if (available != null) {
                InvalidVersionException versionEx = new InvalidVersionException(requestedVersion, basePath, available);
                return handleExceptionInternal(versionEx, null, headers, versionEx.getStatus(), request);
            }
        }

        ConcurrentSet<Integer> available = versionRegistryService.getVersionsForPath(requestUri);
        if (available != null && !available.isEmpty()) {
            MissingVersionException versionEx = new MissingVersionException(requestUri, available);
            return handleExceptionInternal(versionEx, null, headers, versionEx.getStatus(), request);
        }

        return handleExceptionInternal(ex, null, headers, status, request);
    }

    @ExceptionHandler(ServerException.class)
    public @NotNull ResponseEntity<?> handleServerException(
            @NotNull ServerException ex,
            @NotNull HttpServletRequest request) {
        HttpStatus status = ex.getStatus();

        if (acceptsHtml(request))
            return htmlResponse(status.value(), status.getReasonPhrase(), ex.getMessage(), request);

        return ResponseEntity.status(status).body(buildErrorBody(status, ex.getMessage(), request));
    }

    @ExceptionHandler(ApiException.class)
    public @NotNull ResponseEntity<?> handleApiException(
            @NotNull ApiException ex,
            @NotNull HttpServletRequest request) {
        int code = ex.getStatus().getCode();
        String reason = ex.getResponse().getReason();

        if (acceptsHtml(request))
            return htmlResponse(code, HttpStatus.valueOf(code).getReasonPhrase(), reason, request, ErrorSource.API);

        return ResponseEntity.status(code).body(buildErrorBody(code, reason, request));
    }

    @ExceptionHandler(Exception.class)
    public @NotNull ResponseEntity<?> handleAll(
            @NotNull Exception ignore,
            @NotNull HttpServletRequest request) {
        if (acceptsHtml(request))
            return htmlResponse(500, "Internal Server Error", "An unexpected error occurred", request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorBody(500, "An unexpected error occurred", request));
    }

    /**
     * Builds the JSON error response body. Subclasses may override to provide a
     * project-specific error response type.
     *
     * @param statusCode the HTTP status code
     * @param reason the error reason message
     * @param request the current request
     * @return the error response body object
     */
    private @NotNull Object buildErrorBody(int statusCode, String reason, @NotNull HttpServletRequest request) {
        return java.util.Map.of(
            "status", statusCode,
            "error", HttpStatus.valueOf(statusCode).getReasonPhrase(),
            "message", reason != null ? reason : HttpStatus.valueOf(statusCode).getReasonPhrase(),
            "path", route(request)
        );
    }

    /**
     * Builds the JSON error response body from a {@link HttpStatusCode}.
     *
     * @param statusCode the HTTP status code
     * @param reason the error reason message
     * @param request the current web request
     * @return the error response body object
     */
    private @NotNull @Unmodifiable Object buildErrorBody(@NotNull HttpStatusCode statusCode, String reason, @NotNull WebRequest request) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        int code = statusCode.value();
        return buildErrorBody(code, reason != null ? reason : HttpStatus.valueOf(code).getReasonPhrase(), servletRequest);
    }

    /**
     * Builds the JSON error response body from a {@link HttpStatus}.
     *
     * @param status the HTTP status
     * @param reason the error reason message
     * @param request the current request
     * @return the error response body object
     */
    private @NotNull Object buildErrorBody(@NotNull HttpStatus status, String reason, @NotNull HttpServletRequest request) {
        return buildErrorBody(status.value(), reason, request);
    }

    private static boolean acceptsHtml(@NotNull HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains("text/html");
    }

    private static @NotNull String html(int code, @NotNull String title, @NotNull String reason, @NotNull HttpServletRequest request) {
        return html(code, title, reason, request, code < 500 ? ErrorSource.CLIENT : ErrorSource.SERVER);
    }

    private static @NotNull String html(int code, @NotNull String title, @NotNull String reason, @NotNull HttpServletRequest request, @NotNull ErrorSource source) {
        return ErrorPageRenderer.render(code, title, reason, route(request), request.getRemoteAddr(), request.getHeader(CLOUDFLARE_RAY_HEADER), source);
    }

    private static @NotNull ResponseEntity<String> htmlResponse(int code, @NotNull String title, @NotNull String reason, @NotNull HttpServletRequest request) {
        return htmlResponse(code, title, reason, request, code < 500 ? ErrorSource.CLIENT : ErrorSource.SERVER);
    }

    private static @NotNull ResponseEntity<String> htmlResponse(int code, @NotNull String title, @NotNull String reason, @NotNull HttpServletRequest request, @NotNull ErrorSource source) {
        return ResponseEntity.status(code)
            .contentType(MediaType.TEXT_HTML)
            .body(html(code, title, reason, request, source));
    }

    private static @NotNull String route(@NotNull HttpServletRequest request) {
        return request.getMethod() + " " + HtmlUtils.htmlEscape(request.getRequestURI());
    }

}
