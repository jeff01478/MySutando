package com.john.mysutando.event;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
public class MessageEvent extends DiscordEvent {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println(event.getMessage());
        JDA jda = event.getJDA();
        onEvent(new TestEvent(jda));
    }

}
