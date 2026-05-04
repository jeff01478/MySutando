package com.john.mysutando.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "discord_guild") // 資料表名稱叫 discord_guild
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class DiscordGuild {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id; // Discord 伺服器 ID

    @Column(name = "name", nullable = false)
    private String name; // 伺服器名稱

    @Column(name = "icon_url")
    private String iconUrl; // 伺服器頭像網址 (做網頁後台時超好用)

    @Column(name = "owner_id", length = 32)
    private String ownerId; // 伺服器擁有者的 ID

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // 🌟 狀態標記：Bot 是否還在這個伺服器裡？

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}