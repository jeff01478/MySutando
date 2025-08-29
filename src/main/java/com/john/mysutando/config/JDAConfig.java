package com.john.mysutando.config;

import java.util.EnumSet;

import com.john.mysutando.event.EventManager;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class JDAConfig {

    private final EventManager eventManager;

    @Value("${bot.token}")
    private String botToken;

    @Bean
    public JDA jda() throws InterruptedException {
        EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES
        );

        JDA jda = JDABuilder.createDefault(botToken)
                .addEventListeners(eventManager.getAllEventListener().toArray())
                .enableIntents(intents)
                .build();

        jda.awaitReady();
        log.info("已啟動Bot");
        return jda;
    }
}
