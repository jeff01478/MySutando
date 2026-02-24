package com.john.mysutando.service;

import java.util.List;

import com.john.mysutando.dto.rq.MessageRq;

public interface DcLogApiService {
    void receiveMessage(MessageRq rq);

    void deleteMessage(String messageId);

    String getLastSyncId(String channelId);

    void uploadBatchMessages(List<MessageRq> messageRqList);

    void finishSyncMode();

    boolean isSyncing();
}
