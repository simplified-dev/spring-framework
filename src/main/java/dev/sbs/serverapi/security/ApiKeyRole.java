package dev.sbs.serverapi.security;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Hierarchical access roles assignable to an API key.
 *
 * <p>Declaration order defines the hierarchy - earlier entries inherit all
 * permissions of later entries. The precomputed {@link #getHierarchy()} map
 * is built once in the static initializer and is immutable thereafter.</p>
 *
 * @see ApiKeyRoleHierarchy
 */
public enum ApiKeyRole {

    DEVELOPER,
    SUPER_ADMIN,
    ADMIN,
    SUPER_MODERATOR,
    MODERATOR,
    SUPER_USER,
    USER,
    LIMITED_ACCESS;

    @Getter
    private static @NotNull ConcurrentMap<ApiKeyRole, ConcurrentSet<ApiKeyRole>> hierarchy = Concurrent.newMap();

    static {
        ConcurrentMap<ApiKeyRole, ConcurrentSet<ApiKeyRole>> hierarchy = Concurrent.newMap();
        ConcurrentList<ApiKeyRole> values = Concurrent.newUnmodifiableList(values());

        for (int i = 0; i < values.size(); i++)
            hierarchy.put(values.get(i), Concurrent.newSet(values.subList(i, values.size())));

        ApiKeyRole.hierarchy = hierarchy.toUnmodifiableMap();
    }

}
