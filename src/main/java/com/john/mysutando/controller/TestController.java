package com.john.mysutando.controller;

import com.john.mysutando.dto.rs.BaseRs;
import com.john.mysutando.dto.rs.VoiceChannelAudioRs;
import com.john.mysutando.event.DiscordEvent;
import com.john.mysutando.event.EventManager;
import com.john.mysutando.service.DiscordAudioService;
import com.john.mysutando.service.audio.GuildAudioReceiveHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.AudioManager;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class TestController {

    private final EventManager eventManager;
    private final JDA jda;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final DiscordAudioService discordAudioService;

    @GetMapping("/test")
    String getGood() {
        return "GOOD";
    }
}
