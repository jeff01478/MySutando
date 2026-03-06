package com.john.mysutando.dto.rq;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinVoiceRq {
    @NotNull(message = "voiceChannelId 怎麼是空的，搞什麼飛機")
    private Long voiceChannelId;

    private boolean startListen = false; // 預設值
}
