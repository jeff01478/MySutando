package com.john.mysutando.exception;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.john.mysutando.event.DiscordEmergencyAlertEvent;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExceptionEventManager extends InterfacedEventManager {

    private final ApplicationEventPublisher eventPublisher;

    // 去覆寫 JDA 的事件發送處理，用來全域捕獲事件拋出的例外
    @Override
    public void handle(@Nonnull GenericEvent event) {
        for (Object listener : getRegisteredListeners()) {
            try {
                if (listener instanceof EventListener eventListener) {
                    eventListener.onEvent(event);
                }
            } catch (Exception e) {
                String eventName = event.getClass().getSimpleName();
                log.error("JDA 全域攔截器捕獲異常！事件類型: {}", eventName, e);

                // 通知猴子
                eventPublisher.publishEvent(new DiscordEmergencyAlertEvent(
                    "JDA 監聽事件炸裂 (" + eventName + ")", e
                ));
            }
        }
    }
}
