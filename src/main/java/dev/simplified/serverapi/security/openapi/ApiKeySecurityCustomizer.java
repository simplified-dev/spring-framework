package dev.simplified.serverapi.security.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.jetbrains.annotations.NotNull;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;

/**
 * Injects the API key security scheme and reusable error response schema into
 * the OpenAPI specification components.
 *
 * <p>Adds an {@code X-API-Key} header-based security scheme and an
 * {@code ErrorResponse} schema matching the JSON structure produced by the
 * error controller.</p>
 */
public class ApiKeySecurityCustomizer implements GlobalOpenApiCustomizer {

    @Override
    public void customise(@NotNull OpenAPI openApi) {
        Components components = openApi.getComponents();

        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components.addSecuritySchemes("X-API-Key", new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name("X-API-Key")
            .description("API key passed in the X-API-Key request header. Required for all protected endpoints."));

        Schema<?> errorSchema = new ObjectSchema()
            .addProperty("status", new IntegerSchema()
                .description("HTTP status code")
                .example(401))
            .addProperty("error", new StringSchema()
                .description("HTTP reason phrase")
                .example("Unauthorized"))
            .addProperty("message", new StringSchema()
                .description("Human-readable error description")
                .example("Missing X-API-Key header"))
            .addProperty("path", new StringSchema()
                .description("HTTP method and request URI")
                .example("GET /hypixel/counts"));

        components.addSchemas("ErrorResponse", errorSchema);
    }

}
