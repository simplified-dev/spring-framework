package dev.sbs.serverapi.config;

import com.google.gson.Gson;
import dev.sbs.serverapi.error.ErrorResponseWriter;
import dev.simplified.gson.GsonSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Framework-level web configuration providing HTTP message converters and the shared
 * {@link ErrorResponseWriter} for the Gson-based ecosystem.
 *
 * <p>Replaces Spring Boot's default Jackson JSON converter with a {@link GsonHttpMessageConverter}
 * at the same position in the chain, leaving the framework's default
 * {@code ByteArrayHttpMessageConverter}, {@code StringHttpMessageConverter}, and
 * {@code ResourceHttpMessageConverter} at their original positions. So:</p>
 * <ul>
 *   <li>Controllers returning plain {@link String} go through {@code StringHttpMessageConverter} -
 *       no Gson quote-wrapping, no need for {@code produces = MediaType.TEXT_PLAIN_VALUE}.</li>
 *   <li>Endpoints returning {@code byte[]} (e.g. SpringDoc's {@code /v3/api-docs}) go through
 *       {@code ByteArrayHttpMessageConverter} - the bytes are written verbatim instead of
 *       Gson serializing each byte as a JSON int.</li>
 *   <li>Real DTOs and {@link java.util.Map}s go through Gson with the project's custom
 *       {@code TypeAdapter} registrations.</li>
 * </ul>
 *
 * <p>If a consumer defines a {@link Gson} {@code @Bean}, it is used automatically.
 * Otherwise a default Gson instance created from {@link GsonSettings#defaults()} is used
 * as a fallback.</p>
 *
 * <p>Security response headers are not set here - Spring Security's
 * {@code HeadersConfigurer} handles {@code X-Content-Type-Options}, HSTS,
 * {@code X-Frame-Options}, and {@code Referrer-Policy} via
 * {@link dev.sbs.serverapi.security.ApiKeySecurityConfig}.</p>
 */
@Configuration
public class ServerWebConfig implements WebMvcConfigurer {

    private final @NotNull Gson gson;

    public ServerWebConfig(@NotNull ObjectProvider<Gson> gsonProvider) {
        this.gson = gsonProvider.getIfAvailable(() -> GsonSettings.defaults().create());
    }

    /**
     * Provides a no-op {@link ErrorController} bean to prevent Spring Boot's
     * {@code BasicErrorController} from registering the default {@code /error} endpoint.
     * The framework's {@link dev.sbs.serverapi.error.ErrorController RestControllerAdvice}
     * handles all error responses.
     *
     * @return a no-op error controller
     */
    @Bean
    public @NotNull ErrorController noOpErrorController() {
        return new ErrorController() {};
    }

    /**
     * Shared response writer used by {@link dev.sbs.serverapi.error.ErrorController} and
     * the Spring Security entry-point and access-denied handlers to render content-negotiated
     * error responses.
     *
     * @return the response writer
     */
    @Bean
    public @NotNull ErrorResponseWriter errorResponseWriter() {
        return new ErrorResponseWriter(this.gson);
    }

    /**
     * Replaces Spring Boot's default Jackson JSON converter with a Gson converter at the
     * same position in the chain. The default {@code String} / {@code byte[]} /
     * {@code Resource} converters keep their precedence over Gson, so non-DTO responses
     * are written verbatim.
     *
     * @param converters the auto-configured converter chain
     */
    @Override
    public void extendMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {
        int insertAt = converters.size();

        for (int i = converters.size() - 1; i >= 0; i--) {
            if (isJacksonJsonConverter(converters.get(i))) {
                insertAt = i;
                converters.remove(i);
            }
        }

        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(this.gson);
        converters.add(insertAt, gsonConverter);
    }

    /**
     * A converter is the Jackson JSON converter (in either Boot 4's Jackson 3 form or the
     * legacy Jackson 2 form) when its class name contains {@code Jackson} and its
     * supported media types include {@code application/json}. Other Jackson converters
     * (XML, CBOR, Smile, YAML) are left in place.
     */
    private static boolean isJacksonJsonConverter(@NotNull HttpMessageConverter<?> converter) {
        if (!converter.getClass().getName().contains("Jackson"))
            return false;

        for (MediaType mediaType : converter.getSupportedMediaTypes()) {
            if (MediaType.APPLICATION_JSON.includes(mediaType))
                return true;
        }

        return false;
    }

}
