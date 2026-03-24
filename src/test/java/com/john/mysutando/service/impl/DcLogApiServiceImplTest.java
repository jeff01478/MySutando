package com.john.mysutando.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.mysutando.dto.rq.MessageRq;
import com.john.mysutando.exception.ApiException;
import com.john.mysutando.util.ApiClient;

class DcLogApiServiceImplTest {

    private ApiClient apiClient;
    private DcLogApiServiceImpl service;

    @BeforeEach
    void setUp() {
        apiClient = mock(ApiClient.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        service = new DcLogApiServiceImpl(apiClient, new ObjectMapper(), eventPublisher);
        ReflectionTestUtils.setField(service, "domain", "http://localhost");
        ReflectionTestUtils.setField(service, "port", "8080");
    }

    @Test
    // 驗證系統仍在同步模式時，新訊息會先進 live buffer 而不是直接發送。
    void receiveMessage_buffersLogsWhileSyncing() {
        service.receiveMessage(new MessageRq());

        Queue<?> liveBufferQueue = getQueue("liveBufferQueue");
        Queue<?> retryQueue = getQueue("retryQueue");

        assertTrue(service.isSyncing());
        assertEquals(1, liveBufferQueue.size());
        assertEquals(0, retryQueue.size());
    }

    @Test
    // 驗證結束同步模式時，暫存的即時訊息會搬移到主重試佇列。
    void finishSyncMode_movesBufferedLogsIntoRetryQueue() {
        service.receiveMessage(new MessageRq());

        service.finishSyncMode();

        Queue<?> liveBufferQueue = getQueue("liveBufferQueue");
        Queue<?> retryQueue = getQueue("retryQueue");

        assertFalse(service.isSyncing());
        assertEquals(0, liveBufferQueue.size());
        assertEquals(1, retryQueue.size());
    }

    @Test
    // 驗證重試排程會成功補送佇列中的建立訊息請求。
    void processRetryQueue_retriesQueuedCreateLogUntilSuccess() {
        MessageRq request = new MessageRq();
        service.receiveMessage(request);
        service.finishSyncMode();

        service.processRetryQueue();

        verify(apiClient).post(
            eq("http://localhost:8080/api/v1/messages"),
            eq(request),
            eq(null),
            eq(Void.class)
        );
        assertEquals(0, getQueue("retryQueue").size());
    }

    @Test
    // 驗證 READY 狀態下 API 發送失敗時，訊息會進入 retry queue。
    void receiveMessage_queuesLogWhenApiCallFailsAfterReady() {
        doThrow(new ApiException("network error")).when(apiClient).post(any(), any(), eq(null), eq(Void.class));

        service.finishSyncMode();
        service.receiveMessage(new MessageRq());

        assertEquals(1, getQueue("retryQueue").size());
    }

    @Test
    // 驗證補送失敗時會保留訊息，等待下一次排程再重試。
    void processRetryQueue_stopsAfterFailureAndKeepsLogForNextRun() {
        MessageRq request = new MessageRq();
        service.receiveMessage(request);
        service.finishSyncMode();

        doThrow(new ApiException("still failing")).when(apiClient).post(any(), any(), eq(null), eq(Void.class));

        service.processRetryQueue();

        verify(apiClient, times(1)).post(any(), any(), eq(null), eq(Void.class));
        assertEquals(1, getQueue("retryQueue").size());
    }

    @SuppressWarnings("unchecked")
    private Queue<Object> getQueue(String fieldName) {
        return (Queue<Object>) ReflectionTestUtils.getField(service, fieldName);
    }
}

