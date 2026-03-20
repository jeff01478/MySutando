package com.john.mysutando.event;

public record SystemShutdownEvent(String reason, int exitCode) {
}
