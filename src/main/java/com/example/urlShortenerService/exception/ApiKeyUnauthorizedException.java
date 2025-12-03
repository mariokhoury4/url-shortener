package com.example.urlShortenerService.exception;

/**
 * Thrown when an API request is missing a required API key
 * or provides an invalid one.
 */
public class ApiKeyUnauthorizedException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param message the exception message
     */
    public ApiKeyUnauthorizedException(final String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the exception message
     * @param cause   the underlying cause
     */
    public ApiKeyUnauthorizedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
