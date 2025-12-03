package com.example.urlShortenerService.integration;

import com.example.urlShortenerService.client.database.UrlRepository;
import com.example.urlShortenerService.model.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class UrlActivityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;  // adjust package if needed

    @BeforeEach
    void cleanDb() {
        urlRepository.deleteAll();
    }

    @Test
    void givenValidInput_whenCreateUrl_thenReturnOutput() throws Exception {
        String body = """
        {
          "targetUrl": "https://example.com/test"
        }
        """;

        mockMvc.perform(post("/links")
                        .header("X-API-KEY", "dev-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())                          // or .isCreated() if you switch later
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.targetUrl").value("https://example.com/test"));
    }

    @Test
    void givenNoCustomAlias_whenCreateUrl_thenShortCodeGenerated() throws Exception {
        String body = """
        {
          "targetUrl": "https://example.com/generated"
        }
        """;

        mockMvc.perform(post("/links")
                        .header("X-API-KEY", "dev-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetUrl").value("https://example.com/generated"))
                .andExpect(jsonPath("$.shortCode").isString())
                .andExpect(jsonPath("$.shortCode").isNotEmpty());
    }

    @Test
    void givenInvalidUrl_whenCreateUrl_then400() throws Exception {
        String body = """
        {
          "targetUrl": "invalid-url"
        }
        """;

        mockMvc.perform(post("/links")
                        .header("X-API-KEY", "dev-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenDuplicateCustomAlias_whenCreateUrl_then409() throws Exception {
        // seed DB with an existing alias
        Url existing = new Url();
        existing.setCustomAlias("my-alias");
        existing.setTargetUrl("https://example.com");
        existing.setCreatedAt(LocalDateTime.now());
        urlRepository.save(existing);

        // attempt to create with same custom alias
        String body = """
        {
          "targetUrl": "https://google.com",
          "customAlias": "my-alias"
        }
        """;

        mockMvc.perform(post("/links")
                        .header("X-API-KEY", "dev-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void givenExistingShortCode_whenRedirect_then302AndLocationHeader() throws Exception {
        Url url = new Url();
        url.setCustomAlias("xyz789");
        url.setTargetUrl("https://google.com");
        url.setCreatedAt(LocalDateTime.now());
        urlRepository.save(url);

        mockMvc.perform(get("/r/{shortCode}", "xyz789"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));
    }

    @Test
    void givenExistingShortCode_whenRedirect_thenClickCountIncrementsAndLastAccessedUpdated() throws Exception {
        Url url = new Url();
        url.setCustomAlias("stat123");
        url.setTargetUrl("https://example.com");
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);
        url.setLastAccessedAt(null);
        urlRepository.save(url);

        mockMvc.perform(get("/r/{shortCode}", "stat123"))
                .andExpect(status().isFound());

        Url updated = urlRepository.findByCustomAlias("stat123").orElseThrow();
        assertEquals(1L, updated.getClickCount());
        assertNotNull(updated.getLastAccessedAt());
    }

    @Test
    void givenExpiredShortCode_whenRedirect_then410() throws Exception {
        Url url = new Url();
        url.setCustomAlias("expired1");
        url.setTargetUrl("https://example.com");
        url.setCreatedAt(LocalDateTime.now().minusDays(2));
        url.setExpiresAt(LocalDateTime.now().minusDays(1));
        urlRepository.save(url);

        mockMvc.perform(get("/r/{shortCode}", "expired"))
                .andExpect(status().isNotFound()); // use isNotFound() if you map to 404
    }


    @Test
    void givenNonExistingShortCode_whenRedirect_then404() throws Exception {
        mockMvc.perform(get("/r/{shortCode}", "missing123"))
                .andExpect(status().isNotFound());
    }



    @Test
    void givenExistingShortCode_whenGetLinkDetails_thenReturn200() throws Exception {
        // arrange
        final Url url = new Url();
        url.setCustomAlias("abc123");
        url.setTargetUrl("https://example.com");
        url.setCreatedAt(LocalDateTime.now());
        urlRepository.save(url);

        // act + assert
        mockMvc.perform(get("/links/{shortCode}", "abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.targetUrl").value("https://example.com"));
    }

    @Test
    void givenNonExistingShortCode_whenGetLinkDetails_then404() throws Exception {
        // no URL saved in DB
        mockMvc.perform(get("/links/{shortCode}", "does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenExistingUrls_whenListLinks_thenReturnPagedResult() throws Exception {
        // arrange
        final Url url1 = new Url("https://google.com", "code1",
                LocalDateTime.now().plusDays(1));
        final Url url2 = new Url("https://github.com", "code2",
                LocalDateTime.now().plusDays(1));

        urlRepository.saveAll(List.of(url1, url2));

        // act + assert
        mockMvc.perform(get("/links")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].shortCode").value("code2")) // sorted by createdAt desc
                .andExpect(jsonPath("$.content[1].shortCode").value("code1"));
    }
}
