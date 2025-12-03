package com.example.urlShortenerService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration that registers application interceptors.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        // Apply API key interceptor only to /links endpoint
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/links");
    }
}
