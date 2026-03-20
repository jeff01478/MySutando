package com.john.mysutando.service.impl;

import java.util.List;

import com.john.mysutando.annotation.RequireFeature;
import com.john.mysutando.dto.rq.MessageRq;
import com.john.mysutando.enums.GuildFeature;
import com.john.mysutando.event.DiscordEmergencyAlertEvent;
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
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordSyncServiceImpl implements DiscordSyncService {

    private final DcLogApiService dcLogApiService;
    private final MessageMapper messageMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @Override
    public void syncAllAuthGuildMessage(JDA jda) {
        try {
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
        } catch (Exception e) {
            log.error("訊息同步炸裂: {}", e.getMessage(), e);
            eventPublisher.publishEvent(new DiscordEmergencyAlertEvent("訊息同步炸裂", e, true));
        }
    }

    @Async
    @Override
    public void syncGuildMessageAsync(Guild guild) {
        try {
            this.syncGuildMessage(guild);
        } catch (Exception e) {
            log.error("伺服器 {} 的訊息同步炸裂: {}", guild.getName(), e.getMessage(), e);
            eventPublisher.publishEvent(new DiscordEmergencyAlertEvent("訊息同步炸裂", e, false));
        }
    }

    @RequireFeature(value = GuildFeature.RECORD_MESSAGE)
    public void syncGuildMessage(Guild guild) {
        for (TextChannel channel : guild.getTextChannels()) {
            syncChannelWithRetry(channel);
        }
        log.info("所有頻道歷史資料同步完成！");
    }

    private void syncChannelWithRetry(TextChannel channel) {
        int maxAttempts = 3; // 最大重試次數

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                doSyncChannel(channel); // 開始同步
                return;
            } catch (InsufficientPermissionException e) {
                log.warn("沒有權限讀取頻道 {} 的訊息，略過本頻道", channel.getName());
                return;
            } catch (Exception e) {
                log.warn("頻道 {} 同步失敗 (第 {}/{} 次): {}", channel.getName(), attempt, maxAttempts, e.getMessage());

                if (attempt == maxAttempts) { // 失敗 3 次就直接把錯誤往外處理
                    throw e;
                }

                try {
                    Thread.sleep(2000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void doSyncChannel(TextChannel channel) {
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
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
