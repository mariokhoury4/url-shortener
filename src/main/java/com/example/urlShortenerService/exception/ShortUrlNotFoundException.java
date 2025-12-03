package com.example.urlShortenerService.exception;

/**
 * Short URL Not Found Exception.
 */
public class ShortUrlNotFoundException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param message the exception message
     */
    public ShortUrlNotFoundException(final String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the exception message
     * @param cause   the underlying cause
     */
    public ShortUrlNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
