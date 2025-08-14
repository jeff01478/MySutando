package com.john.mysutando.event;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

public class TestEvent extends Event {
    public TestEvent(@NotNull JDA api) {
        super(api);
    }
}
