package com.example.urlShortenerService.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler for the URL Shortener Service
 */
@Log4j2
@RestControllerAdvice(basePackages = "com.example.urlShortenerService")
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotValidException.class)
    public ResponseEntity<ErrorResponse> handleShortUrlNotValid(final ShortUrlNotValidException ex) {
        log.warn("Invalid URL: {}", ex.getMessage());
        final ErrorResponse body = new ErrorResponse("INVALID_URL", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400
    }

    @ExceptionHandler(AliasConflictException.class)
    public ResponseEntity<ErrorResponse> handleAliasConflict(final AliasConflictException ex) {
        log.warn("Alias conflict: {}", ex.getMessage());
        final ErrorResponse body = new ErrorResponse("ALIAS_CONFLICT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(final ShortUrlNotFoundException ex) {
        log.warn("Short URL not found: {}", ex.getMessage());
        final ErrorResponse body = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
    }

    @ExceptionHandler(ShortUrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(final ShortUrlExpiredException ex) {
        log.warn("Short URL expired: {}", ex.getMessage());
        final ErrorResponse body = new ErrorResponse("EXPIRED_URL", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(body); // 410
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(final Exception ex) {
        log.error("Unexpected error", ex);
        final ErrorResponse body = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body); // 500
    }
}
