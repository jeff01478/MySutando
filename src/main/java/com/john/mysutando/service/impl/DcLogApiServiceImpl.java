package com.john.mysutando.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.mysutando.dto.QueuedLog;
import com.john.mysutando.dto.rq.MessageRq;
import com.john.mysutando.service.DcLogApiService;
import com.john.mysutando.util.ApiClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DcLogApiServiceImpl implements DcLogApiService {

    @Value("${dclog.domain}")
    private String domain;

    @Value("${dclog.port}")
    private String port;

    private static final String MESSAGE_PATH = "/api/v1/messages";
    private static final String BACKUP_FILE = "log_queue_backup.json";

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    private final AtomicBoolean isSyncing = new AtomicBoolean(true); // 預設啟動時是訊息同步中
    private final Queue<QueuedLog> retryQueue = new ConcurrentLinkedQueue<>();
    private final Queue<QueuedLog> liveBufferQueue = new ConcurrentLinkedQueue<>(); // 在訊息同步中的時候用來存放新的事件發送過來的訊息

    private final Object stateLock = new Object(); // 用來確保狀態切換執行續安全的鎖

    @Override
    public void receiveMessage(MessageRq rq) {
        QueuedLog logRequest = QueuedLog.builder()
            .type(QueuedLog.LogType.CREATE)
            .createPayload(rq)
            .build();

        handleIncomingLog(logRequest);
    }

    @Override
    public void deleteMessage(String messageId) {
        QueuedLog logRequest = QueuedLog.builder()
            .type(QueuedLog.LogType.DELETE)
            .deleteId(messageId)
            .build();

        handleIncomingLog(logRequest);
    }

    private void handleIncomingLog(QueuedLog logRequest) {
        synchronized (stateLock) {
            // 如果還在歷史同步階段，把即時訊息關禁閉
            if (isSyncing.get()) {
                liveBufferQueue.offer(logRequest);
                log.info("系統同步中，即時訊息已被放入緩衝區 (緩衝數量: {})", liveBufferQueue.size());
                return;
            }
        }

        // 如果已經 READY 了，就走原本的邏輯
        processOrQueue(logRequest);
    }

    @Override
    public String getLastSyncId(String channelId) {
        String url = getBaseUrl() + "/sync/" + channelId;
        return apiClient.get(url, null, String.class);
    }

    @Override
    public void uploadBatchMessages(List<MessageRq> messageRqList) {
        String url = getBaseUrl() + "/batch";
        apiClient.post(url, messageRqList, null, Void.class);
    }

    private void processOrQueue(QueuedLog logRequest) {
        if (!retryQueue.isEmpty()) { // 檢查暫存有沒有東西，沒東西才可以發送
            retryQueue.offer(logRequest);
            log.info("佇列忙碌中，請求已加入排程 (目前積壓: {})", retryQueue.size());
            return;
        }

        try {
            sendToApi(logRequest);
        } catch (Exception e) {
            log.warn("發送失敗，加入重試佇列: {}", e.getMessage());
            retryQueue.offer(logRequest);
        }
    }

    // 實際執行 HTTP 請求的方法
    private void sendToApi(QueuedLog logRequest) {
        String url = getBaseUrl();

        switch (logRequest.getType()) {
            case CREATE:
                apiClient.post(url, logRequest.getCreatePayload(), null, Void.class);
                break;
            case DELETE:
                String deleteUrl = url + "/" + logRequest.getDeleteId();
                apiClient.delete(deleteUrl, null, Void.class);
                break;
        }
    }

    private String getBaseUrl() {
        return domain + ":" + port + MESSAGE_PATH;
    }

    @Override
    public void finishSyncMode() {
        synchronized (stateLock) {
            log.info("歷史訊息同步完成， {} 筆即時訊息移入主佇列...", liveBufferQueue.size());

            while (!liveBufferQueue.isEmpty()) {
                retryQueue.offer(liveBufferQueue.poll());
            }

            isSyncing.set(false);
            log.info("系統狀態切換為 [READY]");
        }
    }

    @Override
    public boolean isSyncing() {
        return isSyncing.get();
    }

    @Scheduled(fixedDelay = 30000) // 每 30 秒檢查一次並消耗暫存的訊息
    public void processRetryQueue() {
        if (retryQueue.isEmpty()) {
            return;
        }

        log.info("開始消化重試佇列，目前積壓: {} 筆", retryQueue.size());

        while (!retryQueue.isEmpty()) {
            QueuedLog currentLog = retryQueue.peek(); // 先用 peek 試探是否成功，有成功才清掉

            try {
                sendToApi(currentLog);
                retryQueue.poll();
                log.info("補送成功！剩餘: {}", retryQueue.size());

            } catch (Exception e) {
                log.warn("補送失敗 ({})，暫停重試，等待下個週期。", e.getMessage());
                break;
            }
        }
    }

    // 啟動時恢復暫存資料
    @PostConstruct
    public void loadQueueFromFile() {
        File file = new File(BACKUP_FILE);
        if (!file.exists()) {
            return;
        }

        try {
            log.info("發現備份檔案，正在恢復佇列資料...");
            List<QueuedLog> savedLogs = objectMapper.readValue(file, new TypeReference<>(){});

            retryQueue.addAll(savedLogs);
            log.info("成功恢復 {} 筆資料！", savedLogs.size());

            if (file.delete()) {
                log.info("備份檔案已刪除");
            }
        } catch (IOException e) {
            log.error("讀取備份檔案失敗: {}", e.getMessage());
        }
    }

    // 關機時把暫存訊息寫入檔案
    @PreDestroy
    public void saveQueueToFile() {
        if (retryQueue.isEmpty()) {
            return;
        }

        log.info("系統關閉中，正在備份 {} 筆未發送的 Log...", retryQueue.size());
        try {
            objectMapper.writeValue(new File(BACKUP_FILE), retryQueue);
            log.info("備份成功！檔案位置: {}", new File(BACKUP_FILE).getAbsolutePath());
        } catch (IOException e) {
            log.error("備份寫入失敗！資料可能會遺失: {}", e.getMessage());
        }
    }
}
