package dev.simplified.serverapi.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for endpoints that opt out of API versioning. Because no handler
 * here declares the {@code version} attribute, {@code ApiVersionWebConfig}'s predicate
 * evaluates to {@code false} and the {@code /{version}} path prefix is not applied -
 * {@code /default} is reachable at exactly {@code /default}.
 */
@RestController
public class TestUnversionedController {

    @GetMapping("/default")
    public @NotNull ResponseEntity<String> getDefaultHello() {
        return ResponseEntity.ok("Hello from default (unversioned) endpoint!");
    }

}
