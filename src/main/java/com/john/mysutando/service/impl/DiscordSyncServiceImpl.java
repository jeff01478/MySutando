package com.john.mysutando.service.impl;

import java.util.List;

import com.john.mysutando.annotation.RequireFeature;
import com.john.mysutando.dto.rq.MessageRq;
import com.john.mysutando.enums.GuildFeature;
import com.john.mysutando.mapper.MessageMapper;
import com.john.mysutando.service.DcLogApiService;
import com.john.mysutando.service.DiscordSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordSyncServiceImpl implements DiscordSyncService {

    private final DcLogApiService dcLogApiService;
    private final MessageMapper messageMapper;
    private final ApplicationContext applicationContext;

    @Async
    @Override
    public void syncAllAuthGuildMessage(JDA jda) {
        List<String> guildIds = dcLogApiService.getWhitelistIds();
        log.info("whitelist: {}", guildIds);
        for (String guildId: guildIds) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                log.warn("伺服器 id {} 不存在", guildId);
                continue;
            }
            this.syncGuildMessage(guild);
        }
        dcLogApiService.finishSyncMode();
    }

    @Async
    @Override
    public void syncGuildMessageAsync(Guild guild) {
        this.syncGuildMessage(guild);
    }

    @RequireFeature(value = GuildFeature.RECORD_MESSAGE)
    public void syncGuildMessage(Guild guild) {
        for (TextChannel channel : guild.getTextChannels()) {
            syncChannelWithRetry(channel, guild.getJDA());
        }
        log.info("所有頻道歷史資料同步完成！");
    }

    private void syncChannelWithRetry(TextChannel channel, JDA jda) {
        int maxAttempts = 3; // 最大重試次數

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                doSyncChannel(channel); // 開始同步
                return;
            } catch (Exception e) {
                log.warn("頻道 {} 同步失敗 (第 {}/{} 次): {}", channel.getName(), attempt, maxAttempts, e.getMessage());

                if (attempt == maxAttempts) {
                    // 失敗 3 次就關機
                    abortSystem(channel.getName(), jda);
                }

                try {
                    Thread.sleep(2000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void doSyncChannel(TextChannel channel) throws Exception {
        while (true) {
            // 取得頻道在資料庫裡最新的訊息
            String lastSavedMessageId = dcLogApiService.getLastSyncId(channel.getId());
            MessageHistory history;
            if (lastSavedMessageId == null) {
                log.info("頻道 {} 沒有歷史紀錄，略過同步 (或改為全量抓取)。", channel.getName());
                history = channel.getHistoryFromBeginning(100).complete();
            } else {
                // 獲取在那筆 ID 之後的新訊息
                history = channel.getHistoryAfter(lastSavedMessageId, 100).complete();
            }

            if (history.isEmpty()) {
                return;
            }

            List<Message> messageList = history.getRetrievedHistory();
            List<MessageRq> messageRqList = messageList.stream()
                .map(messageMapper::toMessageRq)
                .toList();

            dcLogApiService.uploadBatchMessages(messageRqList);
            log.info("頻道 {} 同步了 {} 筆遺漏訊息。", channel.getName(), history.size());
            Thread.sleep(500);
        }
    }

    // 關機程序
    private void abortSystem(String channelName, JDA jda) {
        log.error("頻道 {} 歷史訊息同步已達最大重試次數 (3次) 皆失敗！", channelName);
        log.error("系統無法達到完美一致狀態，為確保資料安全，強制終止 Bot 啟動程序！");

        // 切斷 JDA 連線
        if (jda != null) {
            jda.shutdownNow();
        }

        // Spring Boot 關閉應用程式 (回傳 exit code 1 代表異常結束)
        int exitCode = SpringApplication.exit(applicationContext, () -> 1);

        // 強制關閉 JVM
        System.exit(exitCode);
    }
}
