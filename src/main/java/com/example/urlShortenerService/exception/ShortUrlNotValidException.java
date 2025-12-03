package com.example.urlShortenerService.exception;

/**
 * Short URL Not Valid Exception.
 */
public class ShortUrlNotValidException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param message the exception message
     */
    public ShortUrlNotValidException(final String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the exception message
     * @param cause   the underlying cause
     */
    public ShortUrlNotValidException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
