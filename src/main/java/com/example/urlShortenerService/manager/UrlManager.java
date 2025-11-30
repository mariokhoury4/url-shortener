package com.example.urlShortenerService.manager;

import com.example.urlShortenerService.exception.ShortUrlExpiredException;
import com.example.urlShortenerService.exception.ShortUrlNotFoundException;
import com.example.urlShortenerService.model.CreateUrlInput;
import com.example.urlShortenerService.model.CreateUrlOutput;
import com.example.urlShortenerService.model.LinkDetailsOutput;
import lombok.NonNull;
import org.springframework.data.domain.Page;

public interface UrlManager {

    /**
     * Create the URL and save it into the local Database.
     * @param createUrlInput the Input parameter
     * @return CreateUrlOutput
     */
    CreateUrlOutput createUrl(@NonNull final CreateUrlInput createUrlInput);

    /**
     * Find the target URL for a given short code.
     *
     * @param shortCode the short code
     * @return the target URL
     *
     * @throws ShortUrlNotFoundException if not found
     * @throws ShortUrlExpiredException if expired
     */
    String getTargetUrl(@NonNull final String shortCode);

    /**
     * Get the links details (ex: expiration date, status, stats etc..)
     * @param shortCode the Url short Code
     * @return the Link stats
     * @throws ShortUrlNotFoundException if it is not found (404)
     */
    LinkDetailsOutput getLinkDetails(@NonNull final String shortCode);

    /**
     * Get all the links that are created
     * @param page the number of pages to retrieve
     * @param size the size of each page
     * @return return all the link details output
     */
    Page<LinkDetailsOutput> listLinks(final int page, final int size);
}