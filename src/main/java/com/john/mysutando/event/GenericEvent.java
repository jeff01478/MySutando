package com.john.mysutando.event;

import org.jetbrains.annotations.NotNull;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class GenericEvent extends DiscordEvent {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("onReady");
    }

}
