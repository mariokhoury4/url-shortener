package com.example.urlShortenerService.client.database;

import com.example.urlShortenerService.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    /**
     * Find actual URL based on the custom Alias.
     * @param customAlias the customAlias
     * @return the URL if found.
     */
    Optional<Url> findByCustomAlias(final String customAlias);
}
