package com.john.mysutando.dto.rs;

import java.util.List;

import com.john.mysutando.model.TextChannelInfo;
import com.john.mysutando.model.VoiceChannelInfo;

import lombok.Data;

@Data
public class ChannelInfoRs {
    private List<TextChannelInfo> allTextChannelInfo;

    private List<VoiceChannelInfo> allVoiceChannelInfo;
}
