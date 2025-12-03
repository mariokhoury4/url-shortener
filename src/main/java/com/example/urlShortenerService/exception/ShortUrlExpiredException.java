package com.example.urlShortenerService.exception;

/**
 * Short URL Expired Exception.
 */
public class ShortUrlExpiredException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param message the exception message
     */
    public ShortUrlExpiredException(final String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the exception message
     * @param cause   the underlying cause
     */
    public ShortUrlExpiredException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
