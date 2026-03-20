package com.john.mysutando.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.john.mysutando.event.SystemShutdownEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemLifecycleManager {

    private final ApplicationContext applicationContext;

    private final JDA jda;

    @EventListener
    public void onSystemShutdown(SystemShutdownEvent event) {
        log.error("準備執行系統停機程序...");
        log.error("關機原因: {}", event.reason());

        try {
            if (jda != null) {
                log.info("正在中斷 JDA 連線，將 Bot 設為離線狀態...");
                jda.shutdownNow();
            }
        } catch (Exception e) {
            log.error("關閉 JDA 時發生錯誤: {}", e.getMessage());
        }

        log.info("正在關閉 Spring Boot 應用程式容器...");
        int code = SpringApplication.exit(applicationContext, event::exitCode);

        log.info("JVM 即將終止，Exit Code: {}", code);
        System.exit(code);
    }
}
