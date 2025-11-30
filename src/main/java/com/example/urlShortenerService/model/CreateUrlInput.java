package com.example.urlShortenerService.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * This class is the Input parameters of the CreateUrl API
 */
@Data
@Builder
public class CreateUrlInput {

    // Required
    @NotBlank(message = "targetUrl is required")
    @Size(max = 2048, message = "targetUrl is too long")
    private String targetUrl;

    // Optional
    @Size(min = 3, max = 50, message = "customAlias must be between 3 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "custom Alias can only contain letters, numbers, hyphens, and underscores"
    )
    private String customAlias;

    // Optional
    @Future(message = "expiresAt must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
}
