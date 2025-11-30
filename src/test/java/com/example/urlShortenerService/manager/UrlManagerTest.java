package com.example.urlShortenerService.manager;

import com.example.urlShortenerService.client.database.UrlRepository;
import com.example.urlShortenerService.config.ShortenerProperties;
import com.example.urlShortenerService.exception.ShortUrlExpiredException;
import com.example.urlShortenerService.exception.ShortUrlNotFoundException;
import com.example.urlShortenerService.model.CreateUrlInput;
import com.example.urlShortenerService.model.CreateUrlOutput;
import com.example.urlShortenerService.model.LinkDetailsOutput;
import com.example.urlShortenerService.model.LinkStatus;
import com.example.urlShortenerService.model.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlManagerTest {

    private static final String TARGET_URL = "http://google.com";

    private static final String CUSTOM_ALIAS = "customAlias";

    private static final LocalDateTime EXPIRED_DATE = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime AFTER_A_YEAR_EXPIRY_DATE = LocalDateTime.now().plusYears(1);
    private static final LocalDateTime AFTER_A_WEEK_EXPIRY_DATE = LocalDateTime.now().plusWeeks(1);


    @Mock
    private UrlRepository dbClient;

    @Mock
    private ShortenerProperties props;

    @InjectMocks
    private UrlManagerImpl urlManager;

    @BeforeEach
    void setUp() {
        lenient().when(props.getRedirectDomain())
                .thenReturn("http://localhost:8080/r/");
    }

    @Test
    public void givenCorrectCreateUrlInput_whenCreateUrl_thenReturnCorrectCreateUrlOutput() {

        // arrange
        final CreateUrlInput createUrlInput = CreateUrlInput
                .builder()
                .customAlias(CUSTOM_ALIAS)
                .expiresAt(AFTER_A_WEEK_EXPIRY_DATE)
                .targetUrl(TARGET_URL)
                .build();
        final Url url = new Url(
                TARGET_URL,
                CUSTOM_ALIAS,
                AFTER_A_WEEK_EXPIRY_DATE
        );
        when(dbClient.save(any(Url.class))).thenReturn(url);

        // test
        final CreateUrlOutput createUrlOutput = urlManager.createUrl(createUrlInput);

        // assert
        verify(dbClient, times(1)).save(any(Url.class));
        assertEquals(AFTER_A_WEEK_EXPIRY_DATE, createUrlOutput.getExpiresAt());
        assertEquals(CUSTOM_ALIAS, createUrlOutput.getShortCode());
        assertEquals(TARGET_URL, createUrlOutput.getTargetUrl());
    }

    @Test
    public void givenCreateUrlWithoutExpirationDateInput_whenCreateUrl_thenReturnCorrectCreateUrlOutput() {

        // arrange
        final CreateUrlInput createUrlInput = CreateUrlInput
                .builder()
                .customAlias(CUSTOM_ALIAS)
                .targetUrl(TARGET_URL)
                .build();
        final Url url = new Url(
                TARGET_URL,
                CUSTOM_ALIAS,
                AFTER_A_YEAR_EXPIRY_DATE
        );
        when(dbClient.save(any(Url.class))).thenReturn(url);

        // test
        final CreateUrlOutput createUrlOutput = urlManager.createUrl(createUrlInput);

        // assert
        verify(dbClient, times(1)).save(any(Url.class));
        assertEquals(AFTER_A_YEAR_EXPIRY_DATE, createUrlOutput.getExpiresAt());
        assertEquals(CUSTOM_ALIAS, createUrlOutput.getShortCode());
        assertEquals(TARGET_URL, createUrlOutput.getTargetUrl());
    }

    @Test
    public void givenCreateUrlWithoutCustomAliasInput_whenCreateUrl_thenReturnCorrectCreateUrlOutput() {

        // arrange
        final CreateUrlInput createUrlInput = CreateUrlInput
                .builder()
                .targetUrl(TARGET_URL)
                .build();
        final Url url = new Url(
                TARGET_URL,
                CUSTOM_ALIAS,
                AFTER_A_YEAR_EXPIRY_DATE
        );
        when(dbClient.save(any(Url.class))).thenReturn(url);

        // test
        final CreateUrlOutput createUrlOutput = urlManager.createUrl(createUrlInput);

        // assert
        verify(dbClient, times(1)).save(any(Url.class));
        assertEquals(AFTER_A_YEAR_EXPIRY_DATE, createUrlOutput.getExpiresAt());
        assertEquals(CUSTOM_ALIAS, createUrlOutput.getShortCode());
        assertEquals(TARGET_URL, createUrlOutput.getTargetUrl());
    }

    @Test
    public void givenShortCode_whenGetTargetUrl_ReturnTargetUrl() {
        // arrange
        final Url url = new Url(
                TARGET_URL,
                CUSTOM_ALIAS,
                AFTER_A_YEAR_EXPIRY_DATE
        );
        when(dbClient.findByCustomAlias(anyString())).thenReturn(Optional.of(url));

        // test
        final String actualTargetUrl = urlManager.getTargetUrl(CUSTOM_ALIAS);

        // assert
        verify(dbClient, times(1)).findByCustomAlias(anyString());
        verify(dbClient, times(1)).save(any(Url.class));
        assertEquals(TARGET_URL, actualTargetUrl);
    }

    @Test
    public void givenInvalidShortCode_whenGetTargetUrl_ThrowShortUrlNotFoundException() {
        // arrange
        when(dbClient.findByCustomAlias(anyString())).thenReturn(Optional.empty());

        // test
        assertThrowsExactly(ShortUrlNotFoundException.class, () -> urlManager.getTargetUrl(CUSTOM_ALIAS),
                "Expected getTargetUrl to throw ShortUrlNotFoundException, but it didn't");
    }

    @Test
    public void givenExpiredShortCode_whenGetTargetUrl_ThrowShortUrlExpiredException() {
        // arrange
        final Url url = new Url(
                TARGET_URL,
                CUSTOM_ALIAS,
                EXPIRED_DATE
        );
        when(dbClient.findByCustomAlias(anyString())).thenReturn(Optional.of(url));

        // test
        assertThrowsExactly(ShortUrlExpiredException.class, () -> urlManager.getTargetUrl(CUSTOM_ALIAS),
                "Expected getTargetUrl to throw ShortUrlExpiredException, but it didn't");
        verify(dbClient, times(0)).save(any(Url.class));
    }

    @Test
    public void givenShortCode_whenGetLinkDetails_ReturnLinkStats() {
        // arrange
        final Url url = new Url(
                TARGET_URL,
                CUSTOM_ALIAS,
                AFTER_A_YEAR_EXPIRY_DATE
        );
        when(dbClient.findByCustomAlias(anyString())).thenReturn(Optional.of(url));

        // test
        final LinkDetailsOutput actualLinkDetailsOutput = urlManager.getLinkDetails(CUSTOM_ALIAS);

        // assert
        verify(dbClient, times(1)).findByCustomAlias(anyString());
        assertEquals(0, actualLinkDetailsOutput.getClickCount());
        assertEquals(LinkStatus.ACTIVE, actualLinkDetailsOutput.getStatus());
    }

    @Test
    public void givenExpiredShortCode_whenGetLinkDetails_ReturnLinkStats() {
        // arrange
        final Url url = new Url(
                TARGET_URL,
                CUSTOM_ALIAS,
                EXPIRED_DATE
        );
        when(dbClient.findByCustomAlias(anyString())).thenReturn(Optional.of(url));

        // test
        final LinkDetailsOutput actualLinkDetailsOutput = urlManager.getLinkDetails(CUSTOM_ALIAS);

        // assert
        verify(dbClient, times(1)).findByCustomAlias(anyString());
        assertEquals(0, actualLinkDetailsOutput.getClickCount());
        assertEquals(LinkStatus.EXPIRED, actualLinkDetailsOutput.getStatus());
    }

    @Test
    public void givenUnavailableShortCode_whenGetLinkDetails_ThrowShortUrlNotFoundException() {
        // arrange
        when(dbClient.findByCustomAlias(anyString())).thenReturn(Optional.empty());

        // test
        assertThrowsExactly(ShortUrlNotFoundException.class, () -> urlManager.getLinkDetails(CUSTOM_ALIAS),
                "Expected getTargetUrl to throw ShortUrlNotFoundException, but it didn't");
    }
}
