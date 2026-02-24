package com.john.mysutando.dto.rq;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class MessageRq {

    private String discordMessageId;

    private String guildId;

    private String channelId;

    private int channelType;

    private String threadParentId;

    private AuthorDto author;

    private String content;

    private List<AttachmentDto> attachments;

    private Instant timestamp;

    @Data
    public static class AuthorDto {
        private String userId;
        private String username;
        private String globalName;
        private String avatarUrl;
        private boolean isBot;
    }

    @Data
    public static class AttachmentDto {
        private String id;
        private String url;
        private String filename;
        private String contentType;
    }
}
