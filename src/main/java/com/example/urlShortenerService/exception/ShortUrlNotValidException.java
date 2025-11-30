package com.example.urlShortenerService.exception;

/**
 * Short Url Not Valid Exception
 */
public class ShortUrlNotValidException extends RuntimeException {
    /**
     * public constructor
     * @param message the exception message
     */
    public ShortUrlNotValidException(final String message) {
        super(message);
    }
}
