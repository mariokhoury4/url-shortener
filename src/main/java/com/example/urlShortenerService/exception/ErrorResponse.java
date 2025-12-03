package com.example.urlShortenerService.exception;

/**
 * Error Response Model.
 *
 * @param code    the error code
 * @param message the error message
 */
public record ErrorResponse(String code, String message) { }
