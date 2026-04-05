package dev.sbs.serverapi.version.openapi;

import dev.sbs.serverapi.version.VersionRegistryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers OpenAPI customizer beans for API versioning documentation.
 *
 * <p>Only active when SpringDoc is on the classpath. The string-based
 * {@link ConditionalOnClass} avoids class loading failures when SpringDoc
 * is absent.</p>
 */
@Configuration
@ConditionalOnClass(name = "org.springdoc.core.customizers.OperationCustomizer")
public class ApiVersionOpenApiConfig {

    @Bean
    public @NotNull ApiVersionOperationCustomizer apiVersionOperationCustomizer() {
        return new ApiVersionOperationCustomizer();
    }

    @Bean
    public @NotNull ApiVersionGroupRegistrar apiVersionGroupRegistrar(
            @NotNull VersionRegistryService versionRegistryService,
            @NotNull ConfigurableListableBeanFactory beanFactory) {
        return new ApiVersionGroupRegistrar(versionRegistryService, beanFactory);
    }

}
