package com.example.urlShortenerService.config;

import com.example.urlShortenerService.exception.ApiKeyUnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that enforces API key authentication on protected endpoints.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final ShortenerProperties props;

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        // Only protect POST /links (you can extend this later)
        final boolean isPost = HttpMethod.POST.matches(request.getMethod());
        final String requestUri = request.getRequestURI();

        if (isPost && "/links".equals(requestUri)) {
            final String providedKey = request.getHeader(API_KEY_HEADER);

            if (providedKey == null || providedKey.isBlank()) {
                log.warn("Missing API key for POST /links");
                throw new ApiKeyUnauthorizedException("Missing API key in X-API-KEY header");
            }

            final String expectedKey = props.getApiKey();
            if (expectedKey == null || expectedKey.isBlank()) {
                log.error("API key is not configured in ShortenerProperties");
                throw new ApiKeyUnauthorizedException("API key configuration is missing");
            }

            if (!expectedKey.equals(providedKey)) {
                log.warn("Invalid API key provided for POST /links");
                throw new ApiKeyUnauthorizedException("Invalid API key");
            }

            log.debug("API key validated successfully for POST /links");
        }

        // Allow the request to proceed
        return true;
    }
}
