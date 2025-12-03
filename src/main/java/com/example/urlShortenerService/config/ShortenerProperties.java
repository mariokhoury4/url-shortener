package com.example.urlShortenerService.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the URL Shortener service.
 * <p>
 * These values are loaded from application.properties or environment variables.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "shortener")
public class ShortenerProperties {

    /**
     * Base domain used to build the final short URL.
     * Example: "http://localhost:8080/r/"
     */
    private String redirectDomain;

    /**
     * Default time-to-live (in days) for generated URLs when no explicit
     * expiration date is provided in the CreateUrlInput.
     * Example: 365 = 1 year
     */
    private int defaultTtlDays = 365; // sensible default
}
