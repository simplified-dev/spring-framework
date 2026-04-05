package dev.sbs.serverapi.version.openapi;

import dev.sbs.serverapi.version.ApiVersion;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.jetbrains.annotations.NotNull;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enriches individual OpenAPI operations based on the {@link ApiVersion} annotation.
 *
 * <p>Resolves the annotation with the same method-then-class precedence used by the
 * version handler mapping. When present, adds version error responses and documents
 * the supported version numbers in the operation description.</p>
 */
public class ApiVersionOperationCustomizer implements OperationCustomizer {

    @Override
    public @NotNull Operation customize(@NotNull Operation operation, @NotNull HandlerMethod handlerMethod) {
        ApiVersion annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), ApiVersion.class);

        if (annotation == null)
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), ApiVersion.class);

        if (annotation == null)
            return operation;

        int[] versions = annotation.value();
        String versionList = Arrays.stream(versions)
            .mapToObj(v -> "v" + v)
            .collect(Collectors.joining(", "));

        String existing = operation.getDescription();
        String versionNote = "\n\n**Supported versions**: " + versionList;
        operation.setDescription(existing != null ? existing + versionNote : versionNote.strip());

        ApiResponses responses = operation.getResponses();

        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        responses.addApiResponse("400", errorResponse(
            "Endpoint requires a version prefix",
            400, "Bad Request", "Endpoint '/hello' requires a version prefix; available versions: [1, 2, 3]"));

        return operation;
    }

    private static @NotNull ApiResponse errorResponse(
            @NotNull String description,
            int status,
            @NotNull String error,
            @NotNull String message) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("status", status);
        example.put("error", error);
        example.put("message", message);
        example.put("path", "GET /example");

        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType(
                "application/json",
                new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                    .example(example)
            ));
    }

}
