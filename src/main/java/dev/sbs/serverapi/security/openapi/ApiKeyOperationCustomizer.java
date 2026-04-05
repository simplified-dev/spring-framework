package dev.sbs.serverapi.security.openapi;

import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.serverapi.security.ApiKeyProtected;
import dev.sbs.serverapi.security.ApiKeyRole;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.jetbrains.annotations.NotNull;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enriches individual OpenAPI operations based on the {@link ApiKeyProtected} annotation.
 *
 * <p>Resolves the annotation with the same method-then-class precedence used by the
 * authentication interceptor. When present, adds a security requirement, error responses
 * for authentication and rate limiting, and - when roles are specified - a permission
 * error response and role documentation in the operation description.</p>
 */
public class ApiKeyOperationCustomizer implements OperationCustomizer {

    @Override
    public @NotNull Operation customize(@NotNull Operation operation, @NotNull HandlerMethod handlerMethod) {
        ApiKeyProtected annotation = handlerMethod.getMethodAnnotation(ApiKeyProtected.class);

        if (annotation == null)
            annotation = handlerMethod.getBeanType().getAnnotation(ApiKeyProtected.class);

        if (annotation == null)
            return operation;

        operation.addSecurityItem(new SecurityRequirement().addList("X-API-Key"));

        ApiResponses responses = operation.getResponses();

        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        responses.addApiResponse("401", errorResponse(
            "Missing or invalid API key",
            401, "Unauthorized", "Missing X-API-Key header"));
        responses.addApiResponse("429", errorResponse(
            "Rate limit exceeded",
            429, "Too Many Requests", "Rate limit exceeded"));

        ApiKeyRole[] requiredPermissions = annotation.requiredPermissions();

        if (requiredPermissions.length > 0) {
            String qualifyingRoles = resolveQualifyingRoles(requiredPermissions);

            responses.addApiResponse("403", errorResponse(
                "Insufficient permissions - requires one of: " + qualifyingRoles,
                403, "Forbidden", "Insufficient permissions"));

            String existing = operation.getDescription();
            String roleNote = "\n\n**Required roles** (any one of): `" + qualifyingRoles + "`";
            operation.setDescription(existing != null ? existing + roleNote : roleNote.strip());
        }

        return operation;
    }

    /**
     * Expands the required permissions into the full set of roles that satisfy the
     * requirement via the {@link ApiKeyRole} hierarchy. A role qualifies if its
     * hierarchy set contains at least one of the required permissions.
     *
     * @param requiredPermissions the permissions declared on the annotation
     * @return a comma-separated list of qualifying role names in hierarchy order
     */
    private static @NotNull String resolveQualifyingRoles(@NotNull ApiKeyRole[] requiredPermissions) {
        return Arrays.stream(ApiKeyRole.values())
            .filter(role -> {
                ConcurrentSet<ApiKeyRole> reachable = ApiKeyRole.getHierarchy().get(role);
                return Arrays.stream(requiredPermissions).anyMatch(reachable::contains);
            })
            .map(ApiKeyRole::name)
            .collect(Collectors.joining(", "));
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
