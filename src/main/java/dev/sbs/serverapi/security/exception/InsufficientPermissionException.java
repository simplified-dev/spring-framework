package dev.sbs.serverapi.security.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a valid API key lacks the required permissions for an endpoint.
 */
public final class InsufficientPermissionException extends SecurityException {

    /**
     * Constructs a new {@code InsufficientPermissionException}.
     */
    public InsufficientPermissionException() {
        super(HttpStatus.FORBIDDEN, "Insufficient permissions");
    }

}
