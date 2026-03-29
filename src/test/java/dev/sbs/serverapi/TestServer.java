package dev.sbs.serverapi;

import dev.sbs.serverapi.config.ServerConfig;
import dev.sbs.serverapi.security.SecurityHeaderInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Minimal Spring Boot application for testing the server-api framework.
 *
 * <p>Boots a lightweight server with API versioning, API key authentication, error
 * handling, and the test controllers in {@code dev.sbs.serverapi.controller}. Uses
 * {@link ServerConfig#builder()} defaults with SpringDoc disabled.</p>
 *
 * <p>Run the {@link #main} method to start the server on port 8080, then exercise
 * the framework features manually:</p>
 * <ul>
 *   <li><b>Versioning</b> - {@code GET /v1/hello}, {@code GET /v2/hello}, {@code GET /v3/hello}</li>
 *   <li><b>Unversioned</b> - {@code GET /default}</li>
 *   <li><b>API key auth</b> - {@code GET /api/basic} with {@code X-API-Key: dev-key-777}</li>
 *   <li><b>Error handling</b> - {@code GET /v99/hello} (invalid version), {@code GET /nonexistent} (404)</li>
 * </ul>
 */
@SpringBootApplication
public class TestServer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityHeaderInterceptor());
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TestServer.class);
        application.setDefaultProperties(
            ServerConfig.builder()
                .withApplicationName("server-api-test")
                .withSpringdocEnabled(false)
                .build()
                .toProperties()
        );
        application.run(args);
    }

}
