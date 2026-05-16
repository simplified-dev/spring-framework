package dev.simplified.serverapi.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller demonstrating Spring Framework 7's
 * {@code @RequestMapping(version = ...)} routing. The {@code /v{version}} prefix is
 * injected automatically by {@code ApiVersionWebConfig} because at least one handler
 * here declares {@code version=} - so {@link #getHelloV1()} is reachable at
 * {@code /v1/hello} without the path declaration repeating {@code /v1/}.
 *
 * <p>Unversioned endpoints belong on a separate controller (see
 * {@link TestUnversionedController}); mixing versioned and unversioned methods on one
 * class would prefix all of them.</p>
 */
@RestController
public class TestVersionController {

    @GetMapping(path = "/hello", version = "1")
    public @NotNull ResponseEntity<String> getHelloV1() {
        return ResponseEntity.ok("Hello from API v1!");
    }

    @GetMapping(path = "/hello", version = "2")
    public @NotNull ResponseEntity<String> getHelloV2() {
        return ResponseEntity.ok("Hello from API v2!");
    }

    @GetMapping(path = "/hello", version = "3")
    public @NotNull ResponseEntity<String> getHelloV3() {
        return ResponseEntity.ok("Hello from API v3!");
    }

    @GetMapping(path = "/data", version = "1")
    public @NotNull ResponseEntity<String> getDataV1() {
        return ResponseEntity.ok("Data for API v1: { id: 1, name: 'Item One' }");
    }

    @GetMapping(path = "/data", version = "2")
    public @NotNull ResponseEntity<String> getDataV2() {
        return ResponseEntity.ok("Data for API v2: { itemId: 1, itemName: 'Item One Updated' }");
    }

}
