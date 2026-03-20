package com.john.mysutando.event.jda;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.john.mysutando.service.DiscordSyncService;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;

@Component
@RequiredArgsConstructor
public class SessionEvent extends DiscordEvent {

    private static final Logger log = LogManager.getLogger(SessionEvent.class);
    private final DiscordSyncService discordSyncService;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        log.info("start onReady event");
        JDA jda = event.getJDA();
        discordSyncService.syncAllAuthGuildMessage(jda);
    }
}
