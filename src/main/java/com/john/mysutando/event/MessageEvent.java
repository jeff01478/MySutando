package com.john.mysutando.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.john.mysutando.service.DiscordTextService;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

@Component
@RequiredArgsConstructor
public class MessageEvent extends DiscordEvent {
    private static final Logger log = LogManager.getLogger(MessageEvent.class);

    private final DiscordTextService discordTextService;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        log.info("start onMessageReceived");
        if (event.getAuthor().isBot() || !event.isFromType(ChannelType.TEXT)) {
            return;
        }
        Message message = event.getMessage();
        discordTextService.recordMessage(message);
    }

    @Override
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        Message message = event.getMessage();
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
    }
}
