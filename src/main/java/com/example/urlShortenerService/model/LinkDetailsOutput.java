package com.example.urlShortenerService.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LinkDetailsOutput {
    private String shortCode;
    private String shortUrl;
    private String targetUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private long clickCount;
    private LocalDateTime lastAccessedAt;
    private LinkStatus status; // "ACTIVE" or "EXPIRED"
}
