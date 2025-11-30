package com.example.urlShortenerService.exception;

/**
 * Exception thrown when there is a conflict with an existing alias
 */
public class AliasConflictException extends RuntimeException {

    public AliasConflictException(final String message) {
        super(message);
    }
}
