package com.john.mysutando.service.impl;

import com.john.mysutando.MyAudioReceiveHandler;
import com.john.mysutando.dto.rs.BaseRs;
import com.john.mysutando.dto.rs.SpeakVoiceRs;
import com.john.mysutando.service.DiscordAiApiService;
import com.john.mysutando.service.DiscordAudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class DiscordAudioServiceImpl implements DiscordAudioService {

    private final JDA jda;
    private final DiscordAiApiService discordAiApiService;
    private final AudioPlayerService audioPlayerService;

    @Value("${stinkingGroup.guildId}")
    private String stinkingGroupId;

    @Value("${stinkingGroup.voiceToTextChannelId}")
    private String stinkingGroupTextChannelId;

    @Override
    public BaseRs joinVoiceChannel(Long guildId, Long voiceChannelId, boolean startListen) {
        BaseRs baseRs = new BaseRs();
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            baseRs.setMessage("找不到bot所屬的伺服器: " + guildId);
            return baseRs;
        }

        VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);

        if (voiceChannel == null) {
            baseRs.setMessage("找不到該語音頻道: " + voiceChannelId);
            return baseRs;
        }

        AudioManager audioManager = guild.getAudioManager();

        AudioChannelUnion audioChannelUnion = audioManager.getConnectedChannel();

        // 確認 bot 是不是已經在目標語音頻道
        if (audioChannelUnion != null) {
            String audioChannelId = audioChannelUnion.getId();
            if (Long.decode(audioChannelId).equals(voiceChannelId)) {
                baseRs.setMessage("bot已經在該語音頻道: " + voiceChannel.getName());
                return baseRs;
            }
        }

        audioManager.openAudioConnection(voiceChannel);

        // 是否開始監聽
        if (startListen) {
            audioManager.setReceivingHandler(new MyAudioReceiveHandler());
        }

        baseRs.setMessage("GOOD JOB!!!");
        return baseRs;
    }

    @Override
    public BaseRs speakVoice(String guildId, String voiceChannelId, String text) {
        SpeakVoiceRs rs = new SpeakVoiceRs();
        rs.setSpeakMessage(text);
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            rs.setMessage("找不到bot所屬的伺服器: " + guildId);
            return rs;
        }

        AudioManager audioManager = guild.getAudioManager();

        // 確保 bot 已經連線到語音頻道
        if (audioManager.getConnectedChannel() == null) {
            if (voiceChannelId == null || voiceChannelId.isEmpty()) {
                rs.setMessage("voiceChannelId 怎麼是空的，搞什麼飛機");
                return rs;
            }
            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);
            if (voiceChannel == null) {
                rs.setMessage("找不到該語音頻道: " + voiceChannelId);
                return rs;
            }
            audioManager.openAudioConnection(voiceChannel);
        }

        audioManager.setSendingHandler(audioPlayerService.getAudioSendHandler());

        audioPlayerService.loadAndPlay(text);

        if (guildId.equals(stinkingGroupId)) {
            TextChannel textChannel = guild.getTextChannelById(stinkingGroupTextChannelId);
            if (textChannel != null) {
                textChannel.sendMessage(text).queue();
            }
        }

        rs.setMessage("GOOD JOB!!!");
        rs.setSpeakMessage(text);
        return rs;
    }

    @Override
    public BaseRs stopListen(Long guildId) {
        BaseRs rs = new BaseRs();
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            rs.setMessage("找不到bot所屬的伺服器: " + guildId);
            return rs;
        }

        AudioManager audioManager = guild.getAudioManager();

        MyAudioReceiveHandler myAudioReceiveHandler = (MyAudioReceiveHandler) audioManager.getReceivingHandler();

        if (myAudioReceiveHandler == null) {
            rs.setMessage("找不到語音紀錄");
            return rs;
        }
        myAudioReceiveHandler.stopAndSaveAsWav("received_audio");
        rs.setMessage("GOOD JOB!!!");
        return rs;
    }

    @Override
    public BaseRs startListen(Long guildId) {
        BaseRs rs = new BaseRs();
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            rs.setMessage("找不到bot所屬的伺服器: " + guildId);
            return rs;
        }

        AudioManager audioManager = guild.getAudioManager();

        if (audioManager.getConnectedChannel() == null) {
            rs.setMessage("請先將 bot 加入語音頻道");
            return rs;
        }

        if (audioManager.getReceivingHandler() != null) {
            rs.setMessage("bot 已經在語音頻道監聽中，若要重新監聽請先停止");
            return rs;
        }

        audioManager.setReceivingHandler(new MyAudioReceiveHandler());

        rs.setMessage("GOOD JOB!!!");
        return rs;
    }
}
