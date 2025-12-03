package com.example.urlShortenerService.exception;

/**
 * Exception thrown when there is a conflict with an existing alias.
 */
public class AliasConflictException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param message the exception message
     */
    public AliasConflictException(final String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the exception message
     * @param cause   the underlying cause
     */
    public AliasConflictException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
