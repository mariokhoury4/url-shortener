package com.example.urlShortenerService.activity;

import com.example.urlShortenerService.exception.ShortUrlExpiredException;
import com.example.urlShortenerService.exception.ShortUrlNotFoundException;
import com.example.urlShortenerService.manager.UrlManager;
import com.example.urlShortenerService.model.CreateUrlInput;
import com.example.urlShortenerService.model.CreateUrlOutput;
import com.example.urlShortenerService.model.LinkDetailsOutput;
import com.example.urlShortenerService.model.LinkStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlActivityTest {
    private static final String SHORT_CODE = "shortCode";
    private static final String LONG_URL = "http://IAmAVeryLongUrl.com/";


    @Mock
    private UrlManager manager;

    @InjectMocks
    private UrlActivity urlActivity;

    @Test
    public void givenCreateUrlInput_whenCreateUrl_thenReturnCorrectOutput() {
        // arrange
        final CreateUrlInput createUrlInput = CreateUrlInput
                .builder()
                .customAlias(SHORT_CODE)
                .targetUrl(LONG_URL)
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build();
        final CreateUrlOutput expectedCreateUrlOutput = CreateUrlOutput
                .builder()
                .id(1L)
                .shortCode(SHORT_CODE)
                .targetUrl(LONG_URL)
                .expiresAt(createUrlInput.getExpiresAt())
                .createdAt(LocalDateTime.now())
                .build();
        when(manager.createUrl(createUrlInput)).thenReturn(expectedCreateUrlOutput);

        // test
        final CreateUrlOutput actualCreateUrlOutput = urlActivity.createUrl(createUrlInput);

        // assert
        verify(manager, times(1)).createUrl(createUrlInput);
        assertEquals(expectedCreateUrlOutput, actualCreateUrlOutput);
    }

    @Test
    public void givenAvailableShortCode_whenRedirect_thenRedirectCorrectly() {
        // arrange
        final ResponseEntity<Void> expectedResponse = ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(LONG_URL))
                .build();
        when(manager.getTargetUrl(SHORT_CODE)).thenReturn(LONG_URL);

        // test
        final ResponseEntity<Void> actualResponse = urlActivity.redirect(SHORT_CODE);

        // assert
        verify(manager, times(1)).getTargetUrl(SHORT_CODE);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void givenExpiredShortCode_whenRedirect_thenThrowsShortUrlExpiredException() {
        // arrange
        when(manager.getTargetUrl(SHORT_CODE))
                .thenThrow(new ShortUrlExpiredException("Short URL has expired"));

        // test + assert
        assertThrowsExactly(ShortUrlExpiredException.class,
                () -> urlActivity.redirect(SHORT_CODE));

        verify(manager, times(1)).getTargetUrl(SHORT_CODE);
    }

    @Test
    public void givenWrongShortCode_whenRedirect_thenThrowsShortUrlNotFoundException() {
        // arrange
        when(manager.getTargetUrl(SHORT_CODE))
                .thenThrow(new ShortUrlNotFoundException("Short URL not found"));

        // test + assert
        assertThrowsExactly(ShortUrlNotFoundException.class,
                () -> urlActivity.redirect(SHORT_CODE));

        verify(manager, times(1)).getTargetUrl(SHORT_CODE);
    }

    @Test
    public void givenAvailableShortCode_whenGetLinkDetails_thenShowCorrectStatistic() {
        // arrange
        final LinkDetailsOutput linkDetailsOutput = LinkDetailsOutput
                .builder()
                .shortCode(SHORT_CODE)
                .targetUrl(LONG_URL)
                .expiresAt(LocalDateTime.now().plusWeeks(1))
                .clickCount(5)
                .shortUrl("http:localhost:8080/" + SHORT_CODE)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now().plusSeconds(5))
                .status(LinkStatus.ACTIVE)
                .build();
        final ResponseEntity<LinkDetailsOutput> expectedResponse = ResponseEntity.ok(linkDetailsOutput);
        when(manager.getLinkDetails(SHORT_CODE)).thenReturn(linkDetailsOutput);

        // test
        final ResponseEntity<LinkDetailsOutput> actualResponse = urlActivity.getLinkDetails(SHORT_CODE);

        // assert
        verify(manager, times(1)).getLinkDetails(SHORT_CODE);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void givenUnavailableShortCode_whenGetLinkDetails_thenThrowsShortUrlNotFoundException() {
        // arrange
        when(manager.getLinkDetails(SHORT_CODE))
                .thenThrow(new ShortUrlNotFoundException("Short URL not found"));

        // test + assert
        assertThrowsExactly(ShortUrlNotFoundException.class,
                () -> urlActivity.getLinkDetails(SHORT_CODE));

        verify(manager, times(1)).getLinkDetails(SHORT_CODE);
    }
}
