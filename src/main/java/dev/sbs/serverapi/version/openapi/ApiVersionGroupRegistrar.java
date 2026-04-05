package dev.sbs.serverapi.version.openapi;

import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.serverapi.version.VersionRegistryService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Dynamically registers {@link GroupedOpenApi} beans based on the discovered API
 * version numbers in {@link VersionRegistryService}.
 *
 * <p>Runs after all singletons are initialized, ensuring the version registry has
 * completed its index build. Registers one group per version (e.g., v1, v2) plus an
 * "All" group. If no versioned endpoints exist, no groups are registered, preserving
 * the default single-spec behavior.</p>
 */
@RequiredArgsConstructor
public class ApiVersionGroupRegistrar implements SmartInitializingSingleton {

    private final @NotNull VersionRegistryService versionRegistryService;
    private final @NotNull ConfigurableListableBeanFactory beanFactory;

    @Override
    public void afterSingletonsInstantiated() {
        ConcurrentSet<Integer> allVersions = versionRegistryService.getAllVersions();

        if (allVersions.isEmpty())
            return;

        for (int version : allVersions) {
            GroupedOpenApi group = GroupedOpenApi.builder()
                .group("v" + version)
                .pathsToMatch("/v" + version + "/**")
                .build();

            beanFactory.registerSingleton("groupedOpenApiV" + version, group);
        }

        GroupedOpenApi allGroup = GroupedOpenApi.builder()
            .group("All")
            .pathsToMatch("/**")
            .build();

        beanFactory.registerSingleton("groupedOpenApiAll", allGroup);
    }

}
