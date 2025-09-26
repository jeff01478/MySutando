package com.john.mysutando.dto.rs;

import lombok.Data;

@Data
public class VoiceChannelAudioRs extends BaseRs{
    byte[] audioData;
}
