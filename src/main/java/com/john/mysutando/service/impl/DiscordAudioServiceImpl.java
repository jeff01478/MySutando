package com.john.mysutando.service.impl;

import java.io.IOException;

import com.john.mysutando.service.audio.AudioPlayerService;
import com.john.mysutando.service.audio.GuildAudioReceiveHandler;
import com.john.mysutando.dto.rs.BaseRs;
import com.john.mysutando.dto.rs.SpeakVoiceRs;
import com.john.mysutando.service.DiscordAudioService;
import com.john.mysutando.service.audio.GuildAudioReceiveHandlerFactory;

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
    private final AudioPlayerService audioPlayerService;
    private final GuildAudioReceiveHandlerFactory guildAudioReceiveHandlerFactory;

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
            GuildAudioReceiveHandler guildAudioReceiveHandler = guildAudioReceiveHandlerFactory.createInstance(guildId);
            audioManager.setReceivingHandler(guildAudioReceiveHandler);
            guildAudioReceiveHandler.startListen();
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
    public BaseRs startRecording(Long guildId) {
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

        GuildAudioReceiveHandler guildAudioReceiveHandler = (GuildAudioReceiveHandler) audioManager.getReceivingHandler();

        if (guildAudioReceiveHandler == null) {
            guildAudioReceiveHandler = guildAudioReceiveHandlerFactory.createInstance(guildId);
            audioManager.setReceivingHandler(guildAudioReceiveHandler);
        }

        guildAudioReceiveHandler.startRecording();

        rs.setMessage("GOOD JOB!!!");
        return rs;
    }

    @Override
    public BaseRs stopRecording(Long guildId) {
        BaseRs rs = new BaseRs();
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            rs.setMessage("找不到bot所屬的伺服器: " + guildId);
            return rs;
        }

        AudioManager audioManager = guild.getAudioManager();

        GuildAudioReceiveHandler guildAudioReceiveHandler = (GuildAudioReceiveHandler) audioManager.getReceivingHandler();

        if (guildAudioReceiveHandler == null) {
            rs.setMessage("找不到語音紀錄");
            return rs;
        }
        guildAudioReceiveHandler.stopRecordingAndSaveAsWav("received_audio");
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

        GuildAudioReceiveHandler guildAudioReceiveHandler = (GuildAudioReceiveHandler) audioManager.getReceivingHandler();

        if (guildAudioReceiveHandler == null) {
            guildAudioReceiveHandler = guildAudioReceiveHandlerFactory.createInstance(guildId);
            audioManager.setReceivingHandler(guildAudioReceiveHandler);
        }

        guildAudioReceiveHandler.startListen();

        rs.setMessage("GOOD JOB!!!");
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

        if (audioManager.getConnectedChannel() == null) {
            rs.setMessage("bot 不在語音頻道");
            return rs;
        }

        GuildAudioReceiveHandler guildAudioReceiveHandler = (GuildAudioReceiveHandler) audioManager.getReceivingHandler();

        if (guildAudioReceiveHandler == null) {
            rs.setMessage("bot 不在語音頻道");
            return rs;
        }

        guildAudioReceiveHandler.stopListen();

        rs.setMessage("GOOD JOB!!!");
        return rs;
    }

    @Override
    public BaseRs leaveVoiceChannel(Long guildId) {
        BaseRs rs = new BaseRs();
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            rs.setMessage("找不到bot所屬的伺服器: " + guildId);
            return rs;
        }

        AudioManager audioManager = guild.getAudioManager();

        if (audioManager.getConnectedChannel() == null) {
            rs.setMessage("bot 不在語音頻道");
            return rs;
        }

        audioManager.closeAudioConnection();

        try {
            if (audioManager.getReceivingHandler() instanceof GuildAudioReceiveHandler guildAudioReceiveHandler) {
                guildAudioReceiveHandler.closeResource();
            }
        } catch (IOException e) {
            log.error("清除資源時發生 I/O 錯誤", e);
        } finally {
            audioManager.setReceivingHandler(null);
        }

        rs.setMessage("GOOD JOB!!!");
        return rs;
    }
}
