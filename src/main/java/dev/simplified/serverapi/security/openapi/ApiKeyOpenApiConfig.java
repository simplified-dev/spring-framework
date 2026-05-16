package dev.simplified.serverapi.security.openapi;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers OpenAPI customizer beans for API key security documentation.
 *
 * <p>Only active when SpringDoc is on the classpath. The string-based
 * {@link ConditionalOnClass} avoids class loading failures when SpringDoc
 * is absent.</p>
 */
@Configuration
@ConditionalOnClass(name = "org.springdoc.core.customizers.OperationCustomizer")
public class ApiKeyOpenApiConfig {

    @Bean
    public @NotNull ApiKeySecurityCustomizer apiKeySecurityCustomizer() {
        return new ApiKeySecurityCustomizer();
    }

    @Bean
    public @NotNull ApiKeyOperationCustomizer apiKeyOperationCustomizer() {
        return new ApiKeyOperationCustomizer();
    }

}
