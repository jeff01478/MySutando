package com.john.mysutando.controller;

import com.john.mysutando.event.EventManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class TestController {

    private final EventManager eventManager;
    private final JDA jda;

    @GetMapping("/test")
    String getGood() {
        log.info("{}", eventManager.getAllEvent());
        long guildId = 1402596588987220050L;
        Guild guild = jda.getGuildById(guildId);

        AudioManager audioManager = guild.getAudioManager();

        AudioChannelUnion audioChannelUnion = audioManager.getConnectedChannel();

        System.out.println(audioChannelUnion);

        System.out.println(audioChannelUnion.getId());

        return "GOOD";
    }
}
