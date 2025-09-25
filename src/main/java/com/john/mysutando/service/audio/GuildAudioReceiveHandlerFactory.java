package com.john.mysutando.service.audio;

import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GuildAudioReceiveHandlerFactory {

    private final ApplicationContext applicationContext;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public GuildAudioReceiveHandler createInstance(Long guildId) {
        return applicationContext.getBean(GuildAudioReceiveHandler.class, simpMessagingTemplate, guildId);
    }
}
