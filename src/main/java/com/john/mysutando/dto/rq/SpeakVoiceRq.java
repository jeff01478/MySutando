package com.john.mysutando.dto.rq;

import lombok.Data;
import lombok.NonNull;

@Data
public class SpeakVoiceRq {
    @NonNull
    private String guildId;

    private String voiceChannelId;

    private String text;
}
