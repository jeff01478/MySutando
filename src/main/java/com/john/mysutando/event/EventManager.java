package com.john.mysutando.event;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class EventManager {

    private final List<DiscordEvent> eventListeners;

    public List<DiscordEvent> getAllEventListener() {
        return eventListeners;
    }

    public String getAllEvent() {
        return eventListeners.toString();
    }
}
