package com.john.mysutando.dto.rs;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DcLogMessageRs {

    private String discordMessageId;

    private String guildId;

    private String channelId;

    private String channelType;

    private String threadParentId;

    private DcLogMessageRs.AuthorSnapshot author;

    private String content;

    private Instant timestamp;

    private List<DcLogMessageRs.AttachmentSnapshot> attachments;

    private Instant deletedAt;

    @Data
    public static class AuthorSnapshot {
        private String userId;
        private String username;
        private String globalName; // 顯示名稱
        private String avatarUrl;
        private boolean isBot;
    }

    @Data
    public static class AttachmentSnapshot {
        private String id;
        private String url;
        private String filename;
        private String contentType; // e.g. "image/png"
    }
}
