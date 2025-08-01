package com.john.mysutando.event;

import java.util.LinkedList;
import java.util.List;

public class EventManager {

    private final List<DiscordEvent> eventListeners = new LinkedList<>();

    public EventManager() {
        this.eventListeners.add(new GenericEvent());
    }

    public List<DiscordEvent> getAllEventListener() {
        return eventListeners;
    }
}
