package com.john.mysutando.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.john.mysutando.dto.rq.MessageRq;
import com.john.mysutando.event.DiscordEmergencyAlertEvent;
import com.john.mysutando.mapper.MessageMapper;
import com.john.mysutando.service.DcLogApiService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

class DiscordSyncServiceImplTest {

    private DcLogApiService dcLogApiService;
    private MessageMapper messageMapper;
    private ApplicationEventPublisher eventPublisher;
    private DiscordSyncServiceImpl service;

    @BeforeEach
    void setUp() {
        dcLogApiService = mock(DcLogApiService.class);
        messageMapper = mock(MessageMapper.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new DiscordSyncServiceImpl(dcLogApiService, messageMapper, eventPublisher);
    }

    @Test
    // 驗證白名單中的有效 guild 會被同步，且整體流程最後會切到 READY。
    void syncAllAuthGuildMessage_processesExistingGuildsAndFinishesSync() {
        DiscordSyncServiceImpl spyService = spy(service);
        JDA jda = mock(JDA.class);
        Guild guild = mock(Guild.class);

        when(dcLogApiService.getWhitelistIds()).thenReturn(List.of("guild-1", "missing-guild"));
        when(jda.getGuildById("guild-1")).thenReturn(guild);
        when(jda.getGuildById("missing-guild")).thenReturn(null);
        doNothing().when(spyService).syncGuildMessage(guild);

        spyService.syncAllAuthGuildMessage(jda);

        verify(spyService).syncGuildMessage(guild);
        verify(dcLogApiService).finishSyncMode();
    }

    @Test
    // 驗證全量同步流程若發生例外，會發布需要停機的緊急告警。
    void syncAllAuthGuildMessage_publishesEmergencyAlertWhenSyncFails() {
        DiscordSyncServiceImpl spyService = spy(service);
        JDA jda = mock(JDA.class);
        Guild guild = mock(Guild.class);
        RuntimeException failure = new RuntimeException("sync failed");

        when(dcLogApiService.getWhitelistIds()).thenReturn(List.of("guild-1"));
        when(jda.getGuildById("guild-1")).thenReturn(guild);
        doThrow(failure).when(spyService).syncGuildMessage(guild);

        spyService.syncAllAuthGuildMessage(jda);

        verify(eventPublisher).publishEvent(any(DiscordEmergencyAlertEvent.class));
    }

    @Test
    // 驗證單一 guild 非同步同步失敗時，會發布告警但不要求停機。
    void syncGuildMessageAsync_publishesEmergencyAlertWhenSyncFails() {
        DiscordSyncServiceImpl spyService = spy(service);
        Guild guild = mock(Guild.class);
        RuntimeException failure = new RuntimeException("boom");

        when(guild.getName()).thenReturn("Test Guild");
        doThrow(failure).when(spyService).syncGuildMessage(guild);

        spyService.syncGuildMessageAsync(guild);

        verify(eventPublisher).publishEvent(any(DiscordEmergencyAlertEvent.class));
    }

    @Test
    // 驗證有同步游標時，會抓取新訊息、做 mapping，並批次上傳到 dc-log。
    void syncGuildMessage_uploadsMappedMessagesUntilHistoryIsEmpty() {
        Guild guild = mock(Guild.class);
        TextChannel channel = mock(TextChannel.class);
        MessageHistory.MessageRetrieveAction retrieveAction = mock(MessageHistory.MessageRetrieveAction.class);
        MessageHistory firstHistory = mock(MessageHistory.class);
        MessageHistory secondHistory = mock(MessageHistory.class);
        Message message = mock(Message.class);
        MessageRq mapped = new MessageRq();

        when(guild.getTextChannels()).thenReturn(List.of(channel));
        when(channel.getId()).thenReturn("channel-1");
        when(dcLogApiService.getLastSyncId("channel-1")).thenReturn("last-id", "last-id");
        when(channel.getHistoryAfter("last-id", 100)).thenReturn(retrieveAction);
        when(retrieveAction.complete()).thenReturn(firstHistory, secondHistory);
        when(firstHistory.isEmpty()).thenReturn(false);
        when(firstHistory.getRetrievedHistory()).thenReturn(List.of(message));
        when(firstHistory.size()).thenReturn(1);
        when(secondHistory.isEmpty()).thenReturn(true);
        when(messageMapper.toMessageRq(message)).thenReturn(mapped);

        service.syncGuildMessage(guild);

        verify(dcLogApiService).uploadBatchMessages(List.of(mapped));
        verify(dcLogApiService, times(2)).getLastSyncId("channel-1");
        verify(messageMapper).toMessageRq(message);
    }

    @Test
    // 驗證沒有同步游標時，會改走從頭抓取頻道歷史訊息的流程。
    void syncGuildMessage_readsFromBeginningWhenNoSyncCursorExists() {
        Guild guild = mock(Guild.class);
        TextChannel channel = mock(TextChannel.class);
        MessageHistory.MessageRetrieveAction retrieveAction = mock(MessageHistory.MessageRetrieveAction.class);
        MessageHistory history = mock(MessageHistory.class);

        when(guild.getTextChannels()).thenReturn(List.of(channel));
        when(channel.getId()).thenReturn("channel-1");
        when(channel.getName()).thenReturn("general");
        when(dcLogApiService.getLastSyncId("channel-1")).thenReturn(null);
        when(channel.getHistoryFromBeginning(100)).thenReturn(retrieveAction);
        when(retrieveAction.complete()).thenReturn(history);
        when(history.isEmpty()).thenReturn(true);

        service.syncGuildMessage(guild);

        verify(channel).getHistoryFromBeginning(100);
        verify(dcLogApiService, times(1)).getLastSyncId("channel-1");
        verify(messageMapper, times(0)).toMessageRq(any(Message.class));
        assertEquals(false, history.size() > 0);
    }
}

