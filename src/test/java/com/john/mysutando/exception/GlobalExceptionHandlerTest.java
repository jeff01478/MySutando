package com.john.mysutando.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import com.john.mysutando.dto.rs.ErrorRs;
import com.john.mysutando.event.DiscordEmergencyAlertEvent;

class GlobalExceptionHandlerTest {

    private ApplicationEventPublisher eventPublisher;
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        handler = new GlobalExceptionHandler(eventPublisher);
    }

    @Test
    // 驗證 IllegalArgumentException 會被轉成 400 與對應的錯誤內容。
    void handleIllegalArgument_returnsBadRequestPayload() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/guilds/1/channels");

        ErrorRs body = handler.handleIllegalArgument(
            new IllegalArgumentException("guild not found"),
            request
        ).getBody();

        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals("Business Logic Error", body.getError());
        assertEquals("guild not found", body.getMessage());
        assertEquals("/api/v1/guilds/1/channels", body.getPath());
    }

    @Test
    // 驗證 IllegalStateException 會被轉成 409 衝突錯誤。
    void handleIllegalState_returnsConflictPayload() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/guilds/1/voice/connection");

        ErrorRs body = handler.handleIllegalState(
            new IllegalStateException("bot 不在語音頻道"),
            request
        ).getBody();

        assertEquals(HttpStatus.CONFLICT.value(), body.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), body.getError());
        assertEquals("bot 不在語音頻道", body.getMessage());
    }

    @Test
    // 驗證未知例外會回 500，並同時發布緊急告警事件。
    void handleAllExceptions_returnsInternalServerErrorAndPublishesAlert() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/guilds");

        ErrorRs body = handler.handleAllExceptions(
            new RuntimeException("boom"),
            request
        ).getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), body.getError());
        assertTrue(body.getMessage().contains("猴子工程師"));
        verify(eventPublisher).publishEvent(any(DiscordEmergencyAlertEvent.class));
    }
}

