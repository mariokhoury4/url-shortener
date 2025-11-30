package com.example.urlShortenerService.model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * This class is the Output of the CreateUrl API that will be returned to the client
 */
@Data
@Builder
public class CreateUrlOutput {
    private Long id;

    private String shortCode;

    private String shortUrl;

    private String targetUrl;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}
