package com.example.urlShortenerService.activity;

import com.example.urlShortenerService.manager.UrlManager;
import com.example.urlShortenerService.model.CreateUrlInput;
import com.example.urlShortenerService.model.CreateUrlOutput;
import com.example.urlShortenerService.model.LinkDetailsOutput;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Url Activity
 */
@Log4j2
@RestController
public class UrlActivity {

    // Url manager
    private final UrlManager manager;

    /**
     * Constructor
     * @param manager the Url manager
     */
    public UrlActivity(final UrlManager manager) {
        this.manager = manager;
    }

    /**
     * Create the shortURL that will redirect to the long URL one.
     * @param createUrlInput the input parameter
     * @return The output
     */
    @PostMapping("/links")
    public ResponseEntity<CreateUrlOutput> createUrl(@Valid @RequestBody final CreateUrlInput createUrlInput) {
        log.info("HTTP POST /links received");
        CreateUrlOutput output = manager.createUrl(createUrlInput);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(output);
    }

    /**
     * Redirect to the Long URL from the short one.
     * @param shortCode the input short code
     * @return redirect to the actual link
     */
    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable final String shortCode) {
        log.info("HTTP GET /r/{} received", shortCode);
        final String targetUrl = manager.getTargetUrl(shortCode);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(targetUrl))
                .build();
    }

    /**
     * Get details about the short link.
     * @param shortCode the input short code
     * @return link details (target URL, creation date, etc.)
     */
    @GetMapping("/links/{shortCode}")
    public ResponseEntity<LinkDetailsOutput> getLinkDetails(@PathVariable final String shortCode) {
        log.info("HTTP GET /links/{} received", shortCode);
        return ResponseEntity.ok(manager.getLinkDetails(shortCode));
    }

    /**
     * Get all the link and return a list of them
     * @param page number of pages to query
     * @param size size of the pages
     * @return the list of links
     */
    @GetMapping("/links")
    public ResponseEntity<Page<LinkDetailsOutput>> listLinks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("HTTP GET /links received");
        return ResponseEntity.ok(manager.listLinks(page, size));
    }

}
