package com.john.mysutando.dto.rq;

import lombok.Data;

@Data
public class SpeakVoiceRq {
    private String voiceChannelId;

    private String text;
}
