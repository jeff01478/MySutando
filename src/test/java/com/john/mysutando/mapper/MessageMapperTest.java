package com.john.mysutando.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.john.mysutando.dto.rq.MessageRq;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

class MessageMapperTest {

    private MessageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MessageMapper();
    }

    @Test
    // 驗證完整 Discord 訊息會被正確轉成包含作者、附件與 thread parent 的 MessageRq。
    void toMessageRq_mapsFullMessageIncludingMemberAttachmentAndThreadParent() {
        Message message = mock(Message.class);
        Member member = mock(Member.class);
        User user = mock(User.class);
        Message.Attachment attachment = mock(Message.Attachment.class);
        ThreadChannel threadChannel = mock(ThreadChannel.class, RETURNS_DEEP_STUBS);
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-24T17:30:00+08:00");

        when(message.getId()).thenReturn("msg-1");
        when(message.getGuildId()).thenReturn("guild-1");
        when(message.getChannelId()).thenReturn("channel-1");
        when(message.getChannelType()).thenReturn(ChannelType.TEXT);
        when(message.getStartedThread()).thenReturn(threadChannel);
        when(threadChannel.getParentMessageChannel().getId()).thenReturn("parent-1");
        when(message.getMember()).thenReturn(member);
        when(message.getAuthor()).thenReturn(user);
        when(member.getEffectiveName()).thenReturn("Guild Nickname");
        when(user.getId()).thenReturn("user-1");
        when(user.getGlobalName()).thenReturn("Global John");
        when(user.getAvatarUrl()).thenReturn("https://avatar.example/john.png");
        when(message.getContentDisplay()).thenReturn("hello world");
        when(message.getAttachments()).thenReturn(List.of(attachment));
        when(attachment.getId()).thenReturn("att-1");
        when(attachment.getProxyUrl()).thenReturn("https://cdn.example/file.png");
        when(attachment.getFileName()).thenReturn("file.png");
        when(attachment.getContentType()).thenReturn("image/png");
        when(message.getTimeCreated()).thenReturn(createdAt);

        MessageRq result = mapper.toMessageRq(message);

        assertEquals("msg-1", result.getDiscordMessageId());
        assertEquals("guild-1", result.getGuildId());
        assertEquals("channel-1", result.getChannelId());
        assertEquals(ChannelType.TEXT.getId(), result.getChannelType());
        assertEquals("parent-1", result.getThreadParentId());
        assertEquals("hello world", result.getContent());
        assertEquals(createdAt.toInstant(), result.getTimestamp());

        assertNotNull(result.getAuthor());
        assertEquals("user-1", result.getAuthor().getUserId());
        assertEquals("Guild Nickname", result.getAuthor().getUsername());
        assertEquals("Global John", result.getAuthor().getGlobalName());
        assertEquals("https://avatar.example/john.png", result.getAuthor().getAvatarUrl());
        assertFalse(result.getAuthor().isBot());

        assertEquals(1, result.getAttachments().size());
        assertEquals("att-1", result.getAttachments().get(0).getId());
        assertEquals("https://cdn.example/file.png", result.getAttachments().get(0).getUrl());
        assertEquals("file.png", result.getAttachments().get(0).getFilename());
        assertEquals("image/png", result.getAttachments().get(0).getContentType());
    }

    @Test
    // 驗證缺少 member 與 thread 時，會正確退回使用 user name 並保留空值欄位。
    void toMessageRq_fallsBackToUserNameWhenMemberAndThreadAreMissing() {
        Message message = mock(Message.class);
        User user = mock(User.class);
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-24T10:00:00Z");

        when(message.getId()).thenReturn("msg-2");
        when(message.getGuildId()).thenReturn("guild-2");
        when(message.getChannelId()).thenReturn("channel-2");
        when(message.getChannelType()).thenReturn(ChannelType.PRIVATE);
        when(message.getStartedThread()).thenReturn(null);
        when(message.getMember()).thenReturn(null);
        when(message.getAuthor()).thenReturn(user);
        when(user.getId()).thenReturn("user-2");
        when(user.getName()).thenReturn("Plain User");
        when(user.getGlobalName()).thenReturn(null);
        when(user.getAvatarUrl()).thenReturn(null);
        when(message.getContentDisplay()).thenReturn("fallback message");
        when(message.getAttachments()).thenReturn(List.of());
        when(message.getTimeCreated()).thenReturn(createdAt);

        MessageRq result = mapper.toMessageRq(message);

        assertEquals(ChannelType.PRIVATE.getId(), result.getChannelType());
        assertEquals("Plain User", result.getAuthor().getUsername());
        assertEquals("user-2", result.getAuthor().getUserId());
        assertEquals("fallback message", result.getContent());
        assertEquals(createdAt.toInstant(), result.getTimestamp());
        assertEquals(null, result.getThreadParentId());
        assertEquals(0, result.getAttachments().size());
    }
}

