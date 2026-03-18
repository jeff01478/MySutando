package com.john.mysutando.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.john.mysutando.event.DiscordEmergencyAlertEvent;
import com.john.mysutando.event.SystemShutdownEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordAlertListener {
    @Value("${bot.admin.notify-channel-id}")
    private String adminChannelId;

    private final JDA jda;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void onEmergencyAlert(DiscordEmergencyAlertEvent event) {
        log.info("發送 discord 錯誤訊息給猴子");
        try {
            TextChannel adminChannel = jda.getTextChannelById(adminChannelId);
            if (adminChannel == null) {
                log.error("找不到管理員頻道，無法發送 Discord 告警！");
                log.error("錯誤內容: {}", event.exception().getMessage(), event.exception());
                return;
            }

            String stackTrace = getStackTraceAsString(event.exception());

            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String alertMsg = String.format(
                """
                    **<@682651533003063409> 猴子工程師你的系統又壞了**
                    **time:** `%s`
                    **context:** %s
                    **message error:** `%s`
                    **stack trace:**
                """,
                time,
                event.context(),
                event.exception().getMessage()
            );

            byte[] fileData = stackTrace.getBytes(StandardCharsets.UTF_8);
            String fileName = "stack_trace_" + System.currentTimeMillis() + ".txt";

            adminChannel.sendMessage(alertMsg)
                .addFiles(FileUpload.fromData(fileData, fileName))
                .complete();

        } catch (Exception e) {
            log.error("發送 Discord 告警失敗: {}", e.getMessage(), e);
        } finally {
            if (event.requireShutdown()) {
                eventPublisher.publishEvent(new SystemShutdownEvent(event.context(), 1));
            }
        }
    }

    private String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
