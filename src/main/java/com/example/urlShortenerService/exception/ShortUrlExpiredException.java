package com.example.urlShortenerService.exception;

/**
 * Short Url Expired Exception
 */
public class ShortUrlExpiredException extends RuntimeException {
    /**
     * public constructor
     * @param message the exception message
     */
    public ShortUrlExpiredException(final String message) {
        super(message);
    }
}
