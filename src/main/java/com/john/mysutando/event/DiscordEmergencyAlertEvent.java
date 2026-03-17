package com.john.mysutando.event;

public record DiscordEmergencyAlertEvent(String context, Throwable exception) {}
