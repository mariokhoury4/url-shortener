package com.example.urlShortenerService.activity;

import com.example.urlShortenerService.manager.UrlManager;
import com.example.urlShortenerService.model.CreateUrlInput;
import com.example.urlShortenerService.model.CreateUrlOutput;
import com.example.urlShortenerService.model.LinkDetailsOutput;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Url Activity
 */
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
    public CreateUrlOutput createUrl(@Valid @RequestBody final CreateUrlInput createUrlInput) {
        return manager.createUrl(createUrlInput);
    }

    /**
     * Redirect to the Long URL from the short one.
     * @param shortCode the input short code
     * @return redirect to the actual link
     */
    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable final String shortCode) {
        final String targetUrl = manager.getTargetUrl(shortCode);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(targetUrl))
                .build();
    }

    /**
     * Redirect to the Long URL from the short one.
     * @param shortCode the input short code
     * @return redirect to the actual link
     */
    @GetMapping("/links/{shortCode}")
    public ResponseEntity<LinkDetailsOutput> getLinkDetails(@PathVariable final String shortCode) {
        return ResponseEntity.ok(manager.getLinkDetails(shortCode));
    }

}
