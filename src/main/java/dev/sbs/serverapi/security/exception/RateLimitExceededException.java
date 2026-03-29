package dev.sbs.serverapi.security.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an API key has exceeded its allowed request rate.
 */
public final class RateLimitExceededException extends SecurityException {

    /**
     * Constructs a new {@code RateLimitExceededException}.
     */
    public RateLimitExceededException() {
        super(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
    }

}
