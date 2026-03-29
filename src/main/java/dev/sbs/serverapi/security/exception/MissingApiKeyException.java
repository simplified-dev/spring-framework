package dev.sbs.serverapi.security.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the {@code X-API-Key} header is absent or empty.
 */
public final class MissingApiKeyException extends SecurityException {

    /**
     * Constructs a new {@code MissingApiKeyException}.
     */
    public MissingApiKeyException() {
        super(HttpStatus.UNAUTHORIZED, "Missing X-API-Key header");
    }

}
