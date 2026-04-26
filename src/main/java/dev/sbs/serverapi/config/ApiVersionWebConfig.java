package dev.sbs.serverapi.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.RequestPath;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Wires Spring Framework 7's path-segment API versioning, gated by a predicate so the
 * resolver only treats the first path segment as a version when it matches the
 * {@code v<digits>} convention. Non-matching paths (like {@code /default} or
 * {@code /api/admin-panel}) bypass version extraction entirely and route through the
 * configured default version.
 *
 * <p>Spring's default {@link org.springframework.web.accept.SemanticApiVersionParser}
 * strips the {@code v} prefix and parses the remainder as semver, so handlers declare
 * {@code @RequestMapping(version = "1")} and the runtime matches {@code GET /v1/hello}
 * correctly.</p>
 */
@Configuration
public class ApiVersionWebConfig implements WebMvcConfigurer {

    private static final @NotNull Pattern V_PREFIXED = Pattern.compile("^/v\\d+(?:/.*)?$");

    @Override
    public void configureApiVersioning(@NotNull ApiVersionConfigurer configurer) {
        Predicate<RequestPath> versionPathPredicate = path ->
            V_PREFIXED.matcher(path.value()).matches();

        configurer
            .setVersionRequired(false)
            .setDefaultVersion("1")
            .usePathSegment(0, versionPathPredicate);
    }

}
