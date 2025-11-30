package com.example.urlShortenerService.manager;

import com.example.urlShortenerService.config.ShortenerProperties;
import com.example.urlShortenerService.exception.AliasConflictException;
import com.example.urlShortenerService.exception.ShortUrlExpiredException;
import com.example.urlShortenerService.exception.ShortUrlNotFoundException;
import com.example.urlShortenerService.model.LinkDetailsOutput;
import com.example.urlShortenerService.model.LinkStatus;
import com.example.urlShortenerService.model.TargetUrl;
import com.example.urlShortenerService.model.Url;
import com.example.urlShortenerService.client.database.UrlRepository;
import com.example.urlShortenerService.model.CreateUrlInput;
import com.example.urlShortenerService.model.CreateUrlOutput;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Manage the logic of the CreateUrl API.
 */
@Log4j2
@Service
public class UrlManagerImpl implements UrlManager {
    /**
     * The default domain
     */
    private static final String DEFAULT_DOMAIN = "http://localhost:8080/r/";

    private final UrlRepository dbClient;
    private final ShortenerProperties props;

    /**
     * Validate the CreateUrlInput
     * @param dbClient the database client
     */
    public UrlManagerImpl(final UrlRepository dbClient, final ShortenerProperties props) {
        this.dbClient = dbClient;
        this.props = props;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateUrlOutput createUrl(@NonNull final CreateUrlInput createUrlInput)  {
        log.info("CreateUrl request: targetUrl={}, customAlias={}",
                createUrlInput.getTargetUrl(),
                createUrlInput.getCustomAlias());

        final TargetUrl targetUrl = new TargetUrl(createUrlInput.getTargetUrl());

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
            log.warn("Alias conflict for customAlias={}", url.getCustomAlias());
            throw new AliasConflictException("The custom alias is already in use: " + url.getCustomAlias());
        }
        log.info("Short URL created: alias={}, id={}", createdUrl.getCustomAlias(), createdUrl.getId());


        // Building the output object that will be returned to the client
        return CreateUrlOutput
                .builder()
                .id(createdUrl.getId())
                .targetUrl(createdUrl.getTargetUrl())
                .shortCode(createdUrl.getCustomAlias())
                .shortUrl(props.getRedirectDomain() + createdUrl.getCustomAlias())
                .expiresAt(createdUrl.getExpiresAt())
                .createdAt(createdUrl.getCreatedAt())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTargetUrl(@NonNull final String shortCode) {
        log.info("Redirect request for alias={}", shortCode);

        // Retrieve the URL from the shortCode
        final Url url = dbClient.findByCustomAlias(shortCode)
                .orElseThrow(() -> {
                    log.warn("Redirect failed: alias={} not found", shortCode);
                    return new ShortUrlNotFoundException("Short URL not found");
                });

        // Check if the URL is expired
        if (url.isExpired()) {
            log.warn("Redirect failed: alias={} is expired", shortCode);
            throw new ShortUrlExpiredException("Short URL has expired");
        }

        url.registerClick();
        dbClient.save(url);

        log.info("Redirect success: alias={} -> {}", shortCode, url.getTargetUrl());
        // Return the targetUrl
        return url.getTargetUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkDetailsOutput getLinkDetails(@NonNull final String shortCode) {
        log.info("LinkDetails request for alias={}", shortCode);
        final Url url = dbClient.findByCustomAlias(shortCode)
                .orElseThrow(() -> {
                    log.warn("LinkDetails not found: alias={}", shortCode);
                    return new ShortUrlNotFoundException("Short URL not found");
                });

        final boolean expired = url.isExpired();
        final LinkStatus status = expired ? LinkStatus.EXPIRED : LinkStatus.ACTIVE;

        log.info("LinkDetails delivered: alias={}, status={}", shortCode, status);
        return LinkDetailsOutput.builder()
                .shortCode(url.getCustomAlias())
                .shortUrl(props.getRedirectDomain() + url.getCustomAlias())
                .targetUrl(url.getTargetUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .clickCount(url.getClickCount())
                .lastAccessedAt(url.getLastAccessedAt())
                .status(status)
                .build();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Page<LinkDetailsOutput> listLinks(final int page, final int size) {
        log.info("Listing all links");
        final Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return dbClient.findAll(pageable)
                .map(url -> LinkDetailsOutput.builder()
                        .shortCode(url.getCustomAlias())
                        .shortUrl(props.getRedirectDomain() + url.getCustomAlias())
                        .targetUrl(url.getTargetUrl())
                        .createdAt(url.getCreatedAt())
                        .expiresAt(url.getExpiresAt())
                        .clickCount(url.getClickCount())
                        .lastAccessedAt(url.getLastAccessedAt())
                        .status(url.isExpired() ? LinkStatus.EXPIRED : LinkStatus.ACTIVE)
                        .build());
    }

}
