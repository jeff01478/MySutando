package com.john.mysutando.event;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.john.mysutando.service.DiscordSyncService;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
//        List<Guild> guilds = jda.getMutualGuilds();
//        guilds.forEach(discordSyncService::startHistoricalSync);
        discordSyncService.startHistoricalSync(jda.getGuildById("682954755890216960"));

//        Guild guild = jda.getGuildById("954950144686710834");
//        List<TextChannel> textChannels = guild.getTextChannels();
//        textChannels.forEach(channel -> System.out.println(channel.getName()));
    }
}
