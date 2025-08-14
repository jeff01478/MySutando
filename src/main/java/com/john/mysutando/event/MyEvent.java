package com.john.mysutando.event;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class MyEvent extends DiscordEvent {
    @Override
    public void onTest(TestEvent event) {
        System.out.println("自訂義Event測試");
        log.info("自訂義Event測試");
    }
}
