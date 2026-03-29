package dev.sbs.serverapi.controller;

import dev.sbs.serverapi.security.ApiKeyProtected;
import dev.sbs.serverapi.security.ApiKeyRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller demonstrating {@link ApiKeyProtected} with {@link ApiKeyRole}
 * role requirements under the {@code /api/} path prefix.
 */
@RestController
@RequestMapping("/api")
public class TestApiKeyController {

    @GetMapping("/admin-panel")
    @ApiKeyProtected(requiredPermissions = { ApiKeyRole.ADMIN })
    public @NotNull ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Welcome, Administrator.");
    }

    @PostMapping("/restart")
    @ApiKeyProtected(requiredPermissions = { ApiKeyRole.DEVELOPER })
    public @NotNull ResponseEntity<String> restartService() {
        return ResponseEntity.ok("Service is restarting...");
    }

    @GetMapping("/basic")
    @ApiKeyProtected(requiredPermissions = { ApiKeyRole.USER })
    public @NotNull ResponseEntity<String> basicAccess() {
        return ResponseEntity.ok("Basic user access granted.");
    }

}
