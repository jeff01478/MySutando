package com.john.mysutando.controller;

import com.john.mysutando.event.DiscordEvent;
import com.john.mysutando.event.EventManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;

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
        return "GOOD";
    }
}
