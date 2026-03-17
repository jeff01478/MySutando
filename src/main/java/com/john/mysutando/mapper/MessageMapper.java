package com.john.mysutando.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.john.mysutando.dto.rq.MessageRq;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

@Component
public class MessageMapper {
    public MessageRq toMessageRq(Message message) {
        MessageRq messageRq = new MessageRq();
        messageRq.setDiscordMessageId(message.getId());
        messageRq.setGuildId(message.getGuildId());
        messageRq.setChannelId(message.getChannelId());
        messageRq.setChannelType(message.getChannelType().getId());
        messageRq.setThreadParentId(getThreadParentId(message));
        messageRq.setAuthor(createAuthor(message));
        messageRq.setContent(message.getContentDisplay());
        messageRq.setAttachments(createAttachments(message));
        messageRq.setTimestamp(message.getTimeCreated().toInstant());
        return messageRq;
    }

    private List<MessageRq.AttachmentDto> createAttachments(Message message) {
        List<MessageRq.AttachmentDto> attachments = new ArrayList<>();
        List<Message.Attachment> messageAttachments = message.getAttachments();
        messageAttachments.forEach(messageAttachment -> {
            MessageRq.AttachmentDto attachmentDto = new MessageRq.AttachmentDto();
            attachmentDto.setId(messageAttachment.getId());
            attachmentDto.setUrl(messageAttachment.getProxyUrl());
            attachmentDto.setFilename(messageAttachment.getFileName());
            attachmentDto.setContentType(messageAttachment.getContentType());
            attachments.add(attachmentDto);
        });
        return attachments;
    }

    private MessageRq.AuthorDto createAuthor(Message message) {
        Member member = message.getMember();
        User user = message.getAuthor();
        MessageRq.AuthorDto author = new MessageRq.AuthorDto();
        author.setAvatarUrl(user.getAvatarUrl());
        String name;
        if (member != null) {
            name = member.getEffectiveName();
        } else {
            name = user.getName();
        }
        author.setUsername(name);
        author.setBot(false);
        author.setGlobalName(user.getGlobalName());
        author.setUserId(user.getId());
        return author;
    }

    private String getThreadParentId(Message message) {
        ThreadChannel threadChannel = message.getStartedThread();
        if (threadChannel != null) {
            return threadChannel.getParentMessageChannel().getId();
        }
        return null;
    }
}
