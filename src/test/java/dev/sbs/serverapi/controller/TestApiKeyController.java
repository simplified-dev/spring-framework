package dev.sbs.serverapi.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller demonstrating {@link PreAuthorize} with role requirements
 * resolved through the {@link dev.sbs.serverapi.security.ApiKeyRole} hierarchy
 * under the {@code /api/} path prefix.
 */
@RestController
@RequestMapping("/api")
public class TestApiKeyController {

    @GetMapping(path = "/admin-panel", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public @NotNull ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Welcome, Administrator.");
    }

    @PostMapping(path = "/restart", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('DEVELOPER')")
    public @NotNull ResponseEntity<String> restartService() {
        return ResponseEntity.ok("Service is restarting...");
    }

    @GetMapping(path = "/basic", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('USER')")
    public @NotNull ResponseEntity<String> basicAccess() {
        return ResponseEntity.ok("Basic user access granted.");
    }

}
