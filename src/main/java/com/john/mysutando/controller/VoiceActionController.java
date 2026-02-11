package com.john.mysutando.controller;

import com.john.mysutando.dto.rq.JoinVoiceRq;
import com.john.mysutando.dto.rq.SpeakVoiceRq;
import com.john.mysutando.dto.rs.BaseRs;
import com.john.mysutando.service.DiscordAudioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "語音操作 API", description = "控制 Bot 的語音連線、說話與錄音")
@RequestMapping(value = "/api/v1/guilds/{guildId}/voice")
public class VoiceActionController {

    private final DiscordAudioService discordAudioService;

    // --- 連線管理 (Connection) ---

    @Operation(summary = "將 Bot 加入語音頻道 (建立連線)")
    @PostMapping("/connection")
    public ResponseEntity<BaseRs> joinVoiceChannel(
        @Parameter(description = "伺服器 ID") @PathVariable Long guildId,
        @RequestBody @Valid JoinVoiceRq rq) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(discordAudioService.joinVoiceChannel(guildId, rq.getVoiceChannelId(), rq.isStartListen()));
    }

    @Operation(summary = "中斷語音連線 (移除連線)")
    @DeleteMapping("/connection")
    public ResponseEntity<BaseRs> leaveVoiceChannel(@PathVariable Long guildId) {
        return ResponseEntity.ok(discordAudioService.leaveVoiceChannel(guildId));
    }

    // --- 說話 (Speech) ---

    @Operation(summary = "讓 Bot 說話 (發送語音)")
    @PostMapping("/speech")
    public ResponseEntity<BaseRs> speak(
        @PathVariable String guildId,
        @RequestBody @Valid SpeakVoiceRq rq) {

        // 這裡可以做一個防呆，如果 URL 的 id 跟 Body 的 id 不一致要報錯，或是直接用 URL 的
        return ResponseEntity.ok(discordAudioService.speakVoice(guildId, rq.getVoiceChannelId(), rq.getText()));
    }

    // --- 錄音 (Recording) ---

    @Operation(summary = "開始錄音")
    @PostMapping("/recording")
    public ResponseEntity<BaseRs> startRecording(@PathVariable Long guildId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(discordAudioService.startRecording(guildId));
    }

    @Operation(summary = "停止錄音")
    @DeleteMapping("/recording")
    public ResponseEntity<BaseRs> stopRecording(@PathVariable Long guildId) {
        return ResponseEntity.ok(discordAudioService.stopRecording(guildId));
    }

    // --- 監聽 (Listening) ---

    @Operation(summary = "開始監聽語音")
    @PostMapping("/listening")
    public ResponseEntity<BaseRs> startListen(@PathVariable Long guildId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(discordAudioService.startListen(guildId));
    }

    @Operation(summary = "停止監聽語音")
    @DeleteMapping("/listening")
    public ResponseEntity<BaseRs> stopListen(@PathVariable Long guildId) {
        return ResponseEntity.ok(discordAudioService.stopListen(guildId));
    }
}
