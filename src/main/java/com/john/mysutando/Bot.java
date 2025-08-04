package com.john.mysutando;

import com.john.mysutando.event.EventManager;
import com.john.mysutando.utils.Env;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Log4j2
public class Bot {

    private final String botToken = Env.getProperty("bot.token");

    void start() throws InterruptedException {

        EventManager eventManager = new EventManager();

        JDABuilder jdaBuilder = JDABuilder.createDefault(botToken)
            .addEventListeners(eventManager.getAllEventListener().toArray())
            .setEventPassthrough(true);

        JDA jda = jdaBuilder.build();

        jda.awaitReady();

        log.info("已啟動Bot");
    }
}
