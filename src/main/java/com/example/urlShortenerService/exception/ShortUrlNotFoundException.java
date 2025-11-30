package com.example.urlShortenerService.exception;

/**
 * Short Url Not found Exception
 */
public class ShortUrlNotFoundException extends RuntimeException {
    /**
     * public constructor
     * @param message the exception message
     */
    public ShortUrlNotFoundException(final String message) {
        super(message);
    }

}
