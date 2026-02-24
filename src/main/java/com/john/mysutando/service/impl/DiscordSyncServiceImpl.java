package com.john.mysutando.service.impl;

import java.util.List;

import com.john.mysutando.annotation.RequireFeature;
import com.john.mysutando.dto.rq.MessageRq;
import com.john.mysutando.enums.GuildFeature;
import com.john.mysutando.mapper.MessageMapper;
import com.john.mysutando.service.DcLogApiService;
import com.john.mysutando.service.DiscordSyncService;
import com.john.mysutando.service.DiscordTextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordSyncServiceImpl implements DiscordSyncService {

    private final DcLogApiService dcLogApiService;
    private final MessageMapper messageMapper;

    @Async
    @Override
    @RequireFeature(value = GuildFeature.RECORD_MESSAGE)
    public void startHistoricalSync(Guild guild) {
        try {
            for (TextChannel channel : guild.getTextChannels()) {
                syncChannel(channel);
            }

            log.info("🎉 所有頻道歷史資料同步完成！");

        } catch (Exception e) {
            log.error("歷史同步過程發生嚴重錯誤", e);
        } finally {
            // 3. 🛡️ 無論同步成功或失敗，一定要解除封印！
            // 不然 Bot 會永遠處於「同步中」，即時訊息永遠出不去
            dcLogApiService.finishSyncMode();
        }
    }

    private void syncChannel(TextChannel channel) {
        try {
            while (true) {
                // 取得頻道在資料庫裡最新的訊息
                String lastSavedMessageId = dcLogApiService.getLastSyncId(channel.getId());
                MessageHistory history;
                if (lastSavedMessageId == null) {
                    log.info("頻道 {} 沒有歷史紀錄，略過同步 (或改為全量抓取)。", channel.getName());
                    history = channel.getHistoryFromBeginning(100).complete();
                } else {
                    // B. 使用 JDA 獲取在那筆 ID "之後" 的新訊息
                    // 注意：Discord API 每次最多只能拿 100 筆，如果缺很多，你需要寫分頁迴圈 (Pagination)
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
        } catch (Exception e) {
            log.error("頻道 {} 同步失敗: {}", channel.getName(), e.getMessage(), e);
            // 發生錯誤就印 Log，繼續同步下一個頻道，不要讓整個迴圈死掉
        }
    }
}
