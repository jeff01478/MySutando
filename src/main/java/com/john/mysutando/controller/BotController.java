package com.john.mysutando.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.john.mysutando.dto.rs.GuildInfoRs;
import com.john.mysutando.service.BotService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bot 相關 API")
@RequestMapping(value = "/api/bot")
public class BotController {

    private final BotService botService;

    @Operation(summary = "取得 bot 所在的伺服器")
    @GetMapping("/getAllGuild")
    public ResponseEntity<List<GuildInfoRs>> getAllGuild() {
        return ResponseEntity.ok(botService.getAllGuild());
    }
}
