package com.john.mysutando.controller;

import com.john.mysutando.dto.rq.SpeakVoiceRq;
import com.john.mysutando.dto.rs.BaseRs;
import com.john.mysutando.service.DiscordAudioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "語音相關API")
@RequestMapping(value = "/api/audio")
public class AudioController {

    private final DiscordAudioService discordAudioService;

    @Operation(summary = "將Bot加入到語音頻道", description = "若希望 bot 加入語音時直接開始監聽語音，要把 startListen 設為 true")
    @GetMapping("/joinVoiceChannel")
    ResponseEntity<BaseRs> joinVoiceChannel(@RequestParam("guildId") Long guildId,
                                            @RequestParam("voiceChannelId") Long voiceChannelId,
                                            @RequestParam("startListen") boolean startListen) {
        BaseRs baseRs = new BaseRs();
        if (guildId == null) {
            baseRs.setMessage("guildId 怎麼是空的，搞什麼飛機");
            return ResponseEntity.badRequest().body(baseRs);
        }

        if (voiceChannelId == null) {
            baseRs.setMessage("voiceChannelId 怎麼是空的，搞什麼飛機");
            return ResponseEntity.badRequest().body(baseRs);
        }

        return ResponseEntity.ok(discordAudioService.joinVoiceChannel(guildId, voiceChannelId, startListen));
    }

    @Operation(summary = "讓bot說話", description = "會照著text內容播放語音，若bot已經在要說話的語音頻道可以不用帶channelId")
    @PostMapping("/speakVoice")
    ResponseEntity<BaseRs> sendTextToVoice(@RequestBody SpeakVoiceRq rq) {
        String guildId = rq.getGuildId();
        String voiceChannelId = rq.getVoiceChannelId();
        String text = rq.getText();

        BaseRs baseRs = new BaseRs();
        if (guildId.isEmpty()) {
            baseRs.setMessage("guildId 怎麼是空的，搞什麼飛機");
            return ResponseEntity.badRequest().body(baseRs);
        }

        return ResponseEntity.ok(discordAudioService.speakVoice(guildId, voiceChannelId, text));
    }

    @Operation(summary = "停止監聽語音頻道，並保存音檔", description = "guildId: 要 bot 停止監聽的伺服器")
    @GetMapping("/stopListen")
    ResponseEntity<BaseRs> stopListen(@RequestParam("guildId") Long guildId) {
        BaseRs rs = new BaseRs();
        if (guildId == null) {
            rs.setMessage("guildId 怎麼是空的，搞什麼飛機");
            return ResponseEntity.badRequest().body(rs);
        }
        return ResponseEntity.ok(discordAudioService.stopListen(guildId));
    }

    @Operation(summary = "開始監聽語音頻道")
    @GetMapping("/startListen")
    ResponseEntity<BaseRs> startListen(@RequestParam("guildId") Long guildId) {
        BaseRs rs = new BaseRs();
        if (guildId == null) {
            rs.setMessage("guildId 怎麼是空的，搞什麼飛機");
            return ResponseEntity.badRequest().body(rs);
        }
        return ResponseEntity.ok(discordAudioService.startListen(guildId));
    }
}
