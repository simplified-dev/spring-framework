package dev.sbs.serverapi.security.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an API key does not match any registered key.
 */
public final class InvalidApiKeyException extends SecurityException {

    /**
     * Constructs a new {@code InvalidApiKeyException}.
     */
    public InvalidApiKeyException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid API key");
    }

}
