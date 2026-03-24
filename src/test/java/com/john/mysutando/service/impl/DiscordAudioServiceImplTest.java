package com.john.mysutando.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.john.mysutando.dto.rs.BaseRs;
import com.john.mysutando.service.audio.AudioPlayerService;
import com.john.mysutando.service.audio.GuildAudioReceiveHandler;
import com.john.mysutando.service.audio.GuildAudioReceiveHandlerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

class DiscordAudioServiceImplTest {

    private JDA jda;
    private AudioPlayerService audioPlayerService;
    private GuildAudioReceiveHandlerFactory handlerFactory;
    private DiscordAudioServiceImpl service;

    private Guild guild;
    private AudioManager audioManager;
    private VoiceChannel voiceChannel;
    private GuildAudioReceiveHandler receiveHandler;

    @BeforeEach
    void setUp() {
        jda = mock(JDA.class);
        audioPlayerService = mock(AudioPlayerService.class);
        handlerFactory = mock(GuildAudioReceiveHandlerFactory.class);

        service = new DiscordAudioServiceImpl(jda, audioPlayerService, handlerFactory);
        ReflectionTestUtils.setField(service, "stinkingGroupId", "guild-1");
        ReflectionTestUtils.setField(service, "stinkingGroupTextChannelId", "text-1");

        guild = mock(Guild.class);
        audioManager = mock(AudioManager.class);
        voiceChannel = mock(VoiceChannel.class);
        receiveHandler = mock(GuildAudioReceiveHandler.class);

        when(jda.getGuildById(1L)).thenReturn(guild);
        when(jda.getGuildById("guild-1")).thenReturn(guild);
        when(guild.getAudioManager()).thenReturn(audioManager);
        when(guild.getVoiceChannelById(10L)).thenReturn(voiceChannel);
        when(guild.getVoiceChannelById("10")).thenReturn(voiceChannel);
    }

    @Test
    // 驗證加入語音頻道時會建立連線，且可選擇立即開始監聽。
    void joinVoiceChannel_opensConnectionAndStartsListeningWhenRequested() {
        when(audioManager.getConnectedChannel()).thenReturn(null);
        when(handlerFactory.createInstance(1L)).thenReturn(receiveHandler);

        BaseRs response = service.joinVoiceChannel(1L, 10L, true);

        assertEquals("GOOD JOB!!!", response.getMessage());
        verify(audioManager).openAudioConnection(voiceChannel);
        verify(audioManager).setReceivingHandler(receiveHandler);
        verify(receiveHandler).startListen();
    }

    @Test
    // 驗證 bot 已經在目標語音頻道時，會拒絕重複加入。
    void joinVoiceChannel_rejectsWhenAlreadyConnectedToTargetChannel() {
        AudioChannelUnion connectedChannel = mock(AudioChannelUnion.class);
        when(audioManager.getConnectedChannel()).thenReturn(connectedChannel);
        when(connectedChannel.getId()).thenReturn("10");
        when(voiceChannel.getName()).thenReturn("General");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.joinVoiceChannel(1L, 10L, false)
        );

        assertEquals("bot已經在該語音頻道: General", exception.getMessage());
        verify(audioManager, never()).openAudioConnection(voiceChannel);
    }

    @Test
    // 驗證 speakVoice 在未連線時會自動補連線並啟動播放流程。
    void speakVoice_connectsWhenDisconnectedAndLoadsAudio() {
        AudioSendHandler sendHandler = mock(AudioSendHandler.class);
        when(audioManager.getConnectedChannel()).thenReturn(null);
        when(audioPlayerService.getAudioSendHandler()).thenReturn(sendHandler);

        BaseRs response = service.speakVoice("guild-1", "10", "hello");

        assertInstanceOf(com.john.mysutando.dto.rs.SpeakVoiceRs.class, response);
        verify(audioManager).openAudioConnection(voiceChannel);
        verify(audioManager).setSendingHandler(sendHandler);
        verify(audioPlayerService).loadAndPlay("hello");
    }

    @Test
    // 驗證特定 guild 播音時，會同步將文字送到指定文字頻道。
    void speakVoice_sendsTextMessageToConfiguredTextChannel() {
        AudioSendHandler sendHandler = mock(AudioSendHandler.class);
        TextChannel textChannel = mock(TextChannel.class);
        MessageCreateAction messageAction = mock(MessageCreateAction.class);

        when(audioManager.getConnectedChannel()).thenReturn(mock(AudioChannelUnion.class));
        when(audioPlayerService.getAudioSendHandler()).thenReturn(sendHandler);
        when(guild.getTextChannelById("text-1")).thenReturn(textChannel);
        when(textChannel.sendMessage("hello")).thenReturn(messageAction);

        service.speakVoice("guild-1", "10", "hello");

        verify(textChannel).sendMessage("hello");
        verify(messageAction).queue();
    }

    @Test
    // 驗證未加入語音頻道前，不允許直接開始錄音。
    void startRecording_requiresExistingVoiceConnection() {
        when(audioManager.getConnectedChannel()).thenReturn(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.startRecording(1L)
        );

        assertEquals("請先將 bot 加入語音頻道", exception.getMessage());
    }

    @Test
    // 驗證開始錄音時若缺少 receive handler，會先建立再開始錄音。
    void startRecording_createsReceiveHandlerWhenMissing() {
        when(audioManager.getConnectedChannel()).thenReturn(mock(AudioChannelUnion.class));
        when(audioManager.getReceivingHandler()).thenReturn(null);
        when(handlerFactory.createInstance(1L)).thenReturn(receiveHandler);

        BaseRs response = service.startRecording(1L);

        assertEquals("GOOD JOB!!!", response.getMessage());
        verify(audioManager).setReceivingHandler(receiveHandler);
        verify(receiveHandler).startRecording();
    }

    @Test
    // 驗證 bot 不在語音頻道時，不能停止監聽。
    void stopListen_rejectsWhenBotNotConnected() {
        when(audioManager.getConnectedChannel()).thenReturn(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.stopListen(1L)
        );

        assertEquals("bot 不在語音頻道", exception.getMessage());
    }

    @Test
    // 驗證離開語音頻道時會關閉連線、釋放接收資源並清除 handler。
    void leaveVoiceChannel_closesConnectionAndClearsHandler() throws Exception {
        when(audioManager.getConnectedChannel()).thenReturn(mock(AudioChannelUnion.class));
        when(audioManager.getReceivingHandler()).thenReturn(receiveHandler);

        BaseRs response = service.leaveVoiceChannel(1L);

        assertEquals("GOOD JOB!!!", response.getMessage());
        verify(audioManager).closeAudioConnection();
        verify(receiveHandler).closeResource();
        verify(audioManager).setReceivingHandler(null);
    }
}

