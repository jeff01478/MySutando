package com.john.mysutando.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.john.mysutando.dto.rs.ChannelInfoRs;
import com.john.mysutando.service.GuildService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "伺服器相關 API")
@RequestMapping(value = "/api/guild")
public class GuildController {

    private final GuildService guildService;

    @Operation(summary = "取得伺服器的所有頻道資訊")
    @GetMapping("/getAllChannel")
    public ResponseEntity<ChannelInfoRs> getAllChannel(@RequestParam("guildId") Long guildId) {
        return ResponseEntity.ok(guildService.getAllChannel(guildId));
    }
}
