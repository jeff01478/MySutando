package com.john.mysutando.dto;

import com.john.mysutando.dto.rq.MessageRq;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueuedLog {
    private LogType type;
    private MessageRq createPayload;
    private String deleteId;

    public enum LogType {
        CREATE, DELETE
    }
}
