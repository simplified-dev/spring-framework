package dev.sbs.serverapi.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller demonstrating URL-path-based API versioning. Each handler declares its
 * version both in the {@code path} (so the URL has the {@code /v1/} prefix consumers
 * expect) and via {@code version=} (so Spring Framework 7's
 * {@link org.springframework.web.accept.SemanticApiVersionParser} can populate version
 * metadata for OpenAPI rendering, deprecation/sunset response headers, and content
 * negotiation if a same-path/different-version variant is later added).
 *
 * <p>The {@code /default} endpoint is unversioned and reachable at its base path.</p>
 */
@RestController
public class TestVersionController {

    @GetMapping(path = "/v1/hello", version = "1", produces = MediaType.TEXT_PLAIN_VALUE)
    public @NotNull ResponseEntity<String> getHelloV1() {
        return ResponseEntity.ok("Hello from API v1!");
    }

    @GetMapping(path = "/v2/hello", version = "2", produces = MediaType.TEXT_PLAIN_VALUE)
    public @NotNull ResponseEntity<String> getHelloV2() {
        return ResponseEntity.ok("Hello from API v2!");
    }

    @GetMapping(path = "/v3/hello", version = "3", produces = MediaType.TEXT_PLAIN_VALUE)
    public @NotNull ResponseEntity<String> getHelloV3() {
        return ResponseEntity.ok("Hello from API v3!");
    }

    @GetMapping(path = "/v1/data", version = "1", produces = MediaType.TEXT_PLAIN_VALUE)
    public @NotNull ResponseEntity<String> getDataV1() {
        return ResponseEntity.ok("Data for API v1: { id: 1, name: 'Item One' }");
    }

    @GetMapping(path = "/v2/data", version = "2", produces = MediaType.TEXT_PLAIN_VALUE)
    public @NotNull ResponseEntity<String> getDataV2() {
        return ResponseEntity.ok("Data for API v2: { itemId: 1, itemName: 'Item One Updated' }");
    }

    @GetMapping(path = "/default", produces = MediaType.TEXT_PLAIN_VALUE)
    public @NotNull ResponseEntity<String> getDefaultHello() {
        return ResponseEntity.ok("Hello from default (unversioned) endpoint!");
    }

}
