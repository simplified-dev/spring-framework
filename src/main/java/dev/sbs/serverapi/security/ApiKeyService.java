package dev.sbs.serverapi.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Manages API key validation, rate limiting, and permission resolution.
 *
 * <p>Delegates key lookups to an {@link ApiKeyStore} supplied by the consuming
 * application, so the framework does not own any concrete key source. Permission
 * checks expand the caller's assigned roles through {@link ApiKeyRoleHierarchy}
 * before matching against the required roles.
 */
@Log4j2
@RequiredArgsConstructor
public class ApiKeyService {

    private final @NotNull ApiKeyRoleHierarchy hierarchyService;
    private final @NotNull ApiKeyStore store;

    @PostConstruct
    private void logStore() {
        log.info("ApiKeyService initialized with store: {}", store.getClass().getSimpleName());
    }

    /**
     * Resolves the {@link ApiKey} registered under the given key string.
     *
     * <p>This is the only point in the request path that touches the {@link ApiKeyStore}.
     * Callers should retain the returned instance and pass it to subsequent rate-limit and
     * permission checks rather than re-resolving, so that sliding-window counter state on
     * the {@link ApiKey} is mutated consistently and JPA-backed stores incur a single hit
     * per request.</p>
     *
     * @param apiKey the key string to look up
     * @return the matching {@link ApiKey}, or empty if the key is unregistered
     */
    public @NotNull Optional<ApiKey> resolve(@NotNull String apiKey) {
        return store.findByKey(apiKey);
    }

    /**
     * Checks whether the given {@link ApiKey} holds at least one of the required roles,
     * accounting for role hierarchy expansion.
     *
     * @param key the resolved API key
     * @param requiredPermissions the roles required (any-match semantics)
     * @return {@code true} if {@code requiredPermissions} is empty or the key holds at
     *         least one of the required roles
     */
    public boolean hasPermission(@NotNull ApiKey key, @NotNull ApiKeyRole[] requiredPermissions) {
        if (requiredPermissions.length == 0)
            return true;

        Set<ApiKeyRole> reachable = hierarchyService.getReachablePermissions(key.getPermissions());

        for (ApiKeyRole required : requiredPermissions) {
            if (reachable.contains(required))
                return true;
        }

        return false;
    }

}
