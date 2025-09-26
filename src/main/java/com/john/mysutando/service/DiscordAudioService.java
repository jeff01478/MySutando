package com.john.mysutando.service;

import com.john.mysutando.dto.rs.BaseRs;

public interface DiscordAudioService {
    BaseRs joinVoiceChannel(Long guildId, Long voiceChannelId, boolean startListen);
    BaseRs speakVoice(String guildId, String voiceChannelId, String text);
    BaseRs startRecording(Long guildId);
    BaseRs stopRecording(Long guildId);
    BaseRs startListen(Long guildId);
    BaseRs stopListen(Long guildId);
    BaseRs leaveVoiceChannel(Long guildId);
}
