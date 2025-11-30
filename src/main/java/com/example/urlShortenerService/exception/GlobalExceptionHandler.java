package com.example.urlShortenerService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler for the URL Shortener Service
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotValidException.class)
    public ResponseEntity<ErrorResponse> handleShortUrlNotValid(final ShortUrlNotValidException ex) {
        final ErrorResponse body = new ErrorResponse("INVALID_URL", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400
    }

    @ExceptionHandler(AliasConflictException.class)
    public ResponseEntity<ErrorResponse> handleAliasConflict(final AliasConflictException ex) {
        final ErrorResponse body = new ErrorResponse("ALIAS_CONFLICT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(final ShortUrlNotFoundException ex) {
        final ErrorResponse body = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
    }

    @ExceptionHandler(ShortUrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(final ShortUrlExpiredException ex) {
        final ErrorResponse body = new ErrorResponse("EXPIRED_URL", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(body); // 410
    }
}
