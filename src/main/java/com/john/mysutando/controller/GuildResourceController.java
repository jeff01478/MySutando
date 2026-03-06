package com.john.mysutando.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.john.mysutando.dto.rs.ChannelInfoRs;
import com.john.mysutando.dto.rs.GuildInfoRs;
import com.john.mysutando.service.BotService;
import com.john.mysutando.service.GuildService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "伺服器資源 API", description = "提供 Guild 與 Channel 的資源查詢")
@RequestMapping(value = "/api/v1/guilds")
public class GuildResourceController {

    private final BotService botService;
    private final GuildService guildService;

    @Operation(summary = "取得 Bot 所在的所有伺服器列表")
    @GetMapping
    public ResponseEntity<List<GuildInfoRs>> getGuilds() {
        return ResponseEntity.ok(botService.getAllGuild());
    }

    @Operation(summary = "取得指定伺服器的所有頻道資訊")
    @GetMapping("/{guildId}/channels")
    public ResponseEntity<ChannelInfoRs> getGuildChannels(
        @PathVariable Long guildId) {
        return ResponseEntity.ok(guildService.getAllChannel(guildId));
    }
}
