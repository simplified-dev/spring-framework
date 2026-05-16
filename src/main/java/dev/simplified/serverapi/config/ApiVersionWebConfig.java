package dev.simplified.serverapi.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.server.RequestPath;
import org.springframework.web.accept.SemanticApiVersionParser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Wires Spring Framework 7's path-segment API versioning.
 *
 * <p>Two predicates carry the configuration:</p>
 * <ul>
 *   <li>The {@link PathMatchConfigurer#addPathPrefix} predicate inspects each controller
 *       class at handler-mapping registration and applies a {@code /{version}} prefix to
 *       any controller whose methods declare a non-empty {@code version} attribute. So
 *       {@code @GetMapping(path = "/hello", version = "1")} on a {@code @RestController}
 *       is reachable at {@code /v1/hello} without the path declaration repeating
 *       {@code /v1/}.</li>
 *   <li>The {@link ApiVersionConfigurer#usePathSegment(int, java.util.function.Predicate)}
 *       predicate gates version extraction to URLs that actually start with a
 *       {@code /v<digits>/} segment. Without this, the resolver would try to parse the
 *       first segment of every URL (e.g. {@code "api"} from {@code /api/admin-panel}) and
 *       reject the request with 400.</li>
 * </ul>
 *
 * <p>Spring's default {@link SemanticApiVersionParser}
 * strips the {@code v} prefix and parses the remainder as semver, so handlers declare
 * {@code version = "1"} and {@code GET /v1/foo} routes correctly.</p>
 *
 * <p><b>Constraint:</b> {@link PathMatchConfigurer#addPathPrefix} is class-level - every
 * method on a versioned controller gets the prefix, versioned or not. Don't mix
 * version-bearing methods and unversioned methods in the same controller; put unversioned
 * endpoints in their own class.</p>
 */
@Configuration
public class ApiVersionWebConfig implements WebMvcConfigurer {

    private static final @NotNull Pattern V_PREFIXED = Pattern.compile("^/v\\d+(?:/.*)?$");

    @Override
    public void configureApiVersioning(@NotNull ApiVersionConfigurer configurer) {
        configurer
            .setVersionRequired(false)
            .setDefaultVersion("1")
            .usePathSegment(0, ApiVersionWebConfig::isVersionedPath);
    }

    @Override
    public void configurePathMatch(@NotNull PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/{version}", ApiVersionWebConfig::shouldPrefix);
    }

    private static boolean isVersionedPath(@NotNull RequestPath path) {
        return V_PREFIXED.matcher(path.value()).matches();
    }

    /**
     * Defense-in-depth: never prefix springdoc's own controllers - their {@code /v3/api-docs}
     * and Scalar endpoints must keep their declared paths. See springdoc-openapi
     * <a href="https://github.com/springdoc/springdoc-openapi/issues/3163">issue #3163</a>.
     * Then check the more restrictive condition: does this controller actually have any
     * version-bearing handler methods.
     */
    private static boolean shouldPrefix(@NotNull Class<?> controllerClass) {
        if (controllerClass.getPackageName().startsWith("org.springdoc"))
            return false;

        return hasVersionedMethod(controllerClass);
    }

    private static boolean hasVersionedMethod(@NotNull Class<?> controllerClass) {
        return Arrays.stream(controllerClass.getMethods())
            .map(ApiVersionWebConfig::findRequestMapping)
            .filter(Objects::nonNull)
            .anyMatch(rm -> !rm.version().isEmpty());
    }

    private static RequestMapping findRequestMapping(@NotNull Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
    }

}
