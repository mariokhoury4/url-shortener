package com.example.urlShortenerService.manager;

import com.example.urlShortenerService.exception.AliasConflictException;
import com.example.urlShortenerService.exception.ShortUrlExpiredException;
import com.example.urlShortenerService.exception.ShortUrlNotFoundException;
import com.example.urlShortenerService.exception.ShortUrlNotValidException;
import com.example.urlShortenerService.model.LinkDetailsOutput;
import com.example.urlShortenerService.model.LinkStatus;
import com.example.urlShortenerService.model.Url;
import com.example.urlShortenerService.client.database.UrlRepository;
import com.example.urlShortenerService.model.CreateUrlInput;
import com.example.urlShortenerService.model.CreateUrlOutput;
import lombok.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Manage the logic of the CreateUrl API.
 */
@Service
public class UrlManager {
    /**
     * The default domain
     */
    private static final String DEFAULT_DOMAIN = "http://localhost:8080/r/";

    private final UrlRepository dbClient;

    /**
     * Validate the CreateUrlInput
     * @param dbClient the database client
     */
    public UrlManager(final UrlRepository dbClient) {
        this.dbClient = dbClient;
    }

    /**
     * Create the URL and save it into the local Database.
     * @param createUrlInput the Input parameter
     * @return CreateUrlOutput
     */
    public CreateUrlOutput createUrl(@NonNull final CreateUrlInput createUrlInput)  {
        validateCreateUrlInput(createUrlInput);

        // Create the URL that should be saved in the Database
        final Url url = new Url(
                createUrlInput.getTargetUrl(),
                createUrlInput.getCustomAlias() != null
                        ? createUrlInput.getCustomAlias()
                        : UUID.randomUUID().toString().substring(0, 10),
                createUrlInput.getExpiresAt() != null
                        ? createUrlInput.getExpiresAt()
                        : LocalDateTime.now().plusYears(1));

        // Save the Url to the DB
        final Url createdUrl;

        try {
            createdUrl = dbClient.save(url);
        } catch (final DataIntegrityViolationException e) {
            throw new AliasConflictException("The custom alias is already in use: " + url.getCustomAlias());
        }

        // Building the output object that will be returned to the client
        return CreateUrlOutput
                .builder()
                .id(createdUrl.getId())
                .targetUrl(createdUrl.getTargetUrl())
                .shortCode(createdUrl.getCustomAlias())
                .shortUrl(DEFAULT_DOMAIN + createdUrl.getCustomAlias())
                .expiresAt(createdUrl.getExpiresAt())
                .createdAt(createdUrl.getCreatedAt())
                .build();
    }

    /**
     * Find the target URL for a given short code.
     *
     * @param shortCode the short code
     * @return the target URL
     *
     * @throws ShortUrlNotFoundException if not found
     * @throws ShortUrlExpiredException if expired
     */
    public String getTargetUrl(@NonNull final String shortCode) {
        // Retrieve the URL from the shortCode
        final Url url = dbClient.findByCustomAlias(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));

        // Check if the URL is expired
        if (url.isExpired()) {
            throw new ShortUrlExpiredException("Short URL has expired");
        }

        url.registerClick();
        dbClient.save(url);

        // Return the targetUrl
        return url.getTargetUrl();
    }

    /**
     * Get the links details (ex: expiration date, status, stats etc..)
     * @param shortCode the Url short Code
     * @return the stats
     * @throws ShortUrlNotFoundException if it is not found (404)
     */
    public LinkDetailsOutput getLinkDetails(@NonNull final String shortCode) {
        final Url url = dbClient.findByCustomAlias(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));

        final boolean expired = url.isExpired();
        final LinkStatus status = expired ? LinkStatus.EXPIRED : LinkStatus.ACTIVE;

        return LinkDetailsOutput.builder()
                .shortCode(url.getCustomAlias())
                .shortUrl(DEFAULT_DOMAIN + url.getCustomAlias())
                .targetUrl(url.getTargetUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .clickCount(url.getClickCount())
                .lastAccessedAt(url.getLastAccessedAt())
                .status(status)
                .build();
    }

    /**
     * Validate if the createUrlInput is valid
     * Check if the Long Url is actually a valid url
     * @param createUrlInput
     * @throws ShortUrlNotValidException if the Url is not valide
     */
    private void validateCreateUrlInput(final CreateUrlInput createUrlInput) {
        try {
            new URL(createUrlInput.getTargetUrl()).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new ShortUrlNotValidException("TargetUrl is not a valid URL: " + e);
        }
    }

}
