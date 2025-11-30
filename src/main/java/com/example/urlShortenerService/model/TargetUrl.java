package com.example.urlShortenerService.model;

import com.example.urlShortenerService.exception.ShortUrlNotValidException;

import java.net.URI;
import java.net.URISyntaxException;

public record TargetUrl(String value) {

    public TargetUrl {
        if (value == null || value.isBlank()) {
            throw new ShortUrlNotValidException("Target URL cannot be empty");
        }

        try {
            URI uri = new URI(value);

            if (uri.getScheme() == null ||
                    !(uri.getScheme().equalsIgnoreCase("http") ||
                            uri.getScheme().equalsIgnoreCase("https"))) {
                throw new ShortUrlNotValidException("URL must start with http or https");
            }

            if (uri.getHost() == null) {
                throw new ShortUrlNotValidException("URL must contain a valid host");
            }

        } catch (URISyntaxException e) {
            throw new ShortUrlNotValidException("Invalid URL syntax: " + value);
        }
    }
}
