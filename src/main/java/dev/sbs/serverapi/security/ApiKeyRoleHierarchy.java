package dev.sbs.serverapi.security;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import org.jetbrains.annotations.NotNull;

/**
 * Hierarchical role resolver for API key authorization.
 *
 * <p>Expands a set of assigned {@link ApiKeyRole} roles into the full reachable set
 * using the precomputed hierarchy in {@link ApiKeyRole#getHierarchy()}. Higher roles
 * automatically inherit all permissions of lower roles.</p>
 */
public class ApiKeyRoleHierarchy {

    /**
     * Expands a set of assigned roles into the full reachable set including
     * all inherited roles from the hierarchy.
     *
     * @param assignedPermissions the roles assigned to an API key
     * @return the expanded set including all inherited roles
     */
    public @NotNull ConcurrentSet<ApiKeyRole> getReachablePermissions(@NotNull ConcurrentSet<ApiKeyRole> assignedPermissions) {
        ConcurrentSet<ApiKeyRole> reachable = Concurrent.newSet(assignedPermissions);

        for (ApiKeyRole perm : assignedPermissions)
            reachable.addAll(ApiKeyRole.getHierarchy().get(perm));

        return reachable;
    }

}
