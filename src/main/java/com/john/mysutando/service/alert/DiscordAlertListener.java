package com.john.mysutando.service.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.john.mysutando.event.DiscordEmergencyAlertEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordAlertListener {
    @Value("${bot.admin.notify-channel-id}")
    private String adminChannelId;

    private final JDA jda;

    @Async
    @EventListener
    public void onEmergencyAlert(DiscordEmergencyAlertEvent event) {
        try {
            TextChannel adminChannel = jda.getTextChannelById(adminChannelId);
            if (adminChannel == null) {
                log.error("找不到管理員頻道，無法發送 Discord 告警！");
                log.error("錯誤內容: {}", event.exception().getMessage(), event.exception());
                return;
            }

            String stackTrace = getStackTraceAsString(event.exception());

            if (stackTrace.length() > 1000) {
                stackTrace = stackTrace.substring(0, 1000) + "\n......";
            }

            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String alertMsg = String.format(
                """
                    **<@682651533003063409> 猴子工程師你的系統又壞了**
                    **time:** `%s`
                    **context:** %s
                    **message error:** `%s`
                    **stack trace:**
                    ```java
                    %s
                    ```
                """,
                time,
                event.context(),
                event.exception().getMessage(),
                stackTrace
            );

            adminChannel.sendMessage(alertMsg).queue();

        } catch (Exception e) {
            log.error("發送 Discord 告警失敗: {}", e.getMessage(), e);
        }
    }

    private String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
