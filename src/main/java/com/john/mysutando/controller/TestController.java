package com.john.mysutando.controller;

import com.john.mysutando.event.DiscordEvent;
import com.john.mysutando.event.EventManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

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
        Guild guild = jda.getGuildById("682954755890216960");

        System.out.println(guild.getMembers());
//
//        Member member = guild.getMemberById("682651533003063409");
//
//        User user = member.getUser();
//
//        System.out.println(user.getMutualGuilds());

//        User user = jda.getUserById("682651533003063409");
//        System.out.println(user);
//        System.out.println(user.getMutualGuilds());
        return "GOOD";
    }
}
