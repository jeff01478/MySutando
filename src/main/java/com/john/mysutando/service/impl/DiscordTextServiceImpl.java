package com.john.mysutando.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.john.mysutando.annotation.RequireFeature;
import com.john.mysutando.dto.rq.MessageRq;
import com.john.mysutando.enums.GuildFeature;
import com.john.mysutando.mapper.MessageMapper;
import com.john.mysutando.service.DcLogApiService;
import com.john.mysutando.service.DiscordTextService;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Service
@RequiredArgsConstructor
public class DiscordTextServiceImpl implements DiscordTextService {

    private static final String WEBHOOK_NAME = "MySutando";
    private static final Logger log = LogManager.getLogger(DiscordTextServiceImpl.class);

    private final DcLogApiService dcLogApiService;
    private final MessageMapper messageMapper;

    @Override
    @RequireFeature(value = GuildFeature.IMPERSONATION)
    public void impersonateMember(Member member, TextChannel channel, String message) {
        channel.retrieveWebhooks().queue(webhooks -> {
            Webhook targetWebhook = webhooks.stream()
                .filter(wh -> wh.getOwnerAsUser().getId().equals(channel.getJDA().getSelfUser().getId()))
                .findFirst()
                .orElse(null);

            if (targetWebhook == null) {
                // 沒有就建立一個
                channel.createWebhook(WEBHOOK_NAME).queue(webhook -> doImpersonate(webhook, member, message));
            } else {
                // 有就直接用
                doImpersonate(targetWebhook, member, message);
            }
        });
    }

    @Override
    @RequireFeature(GuildFeature.RECORD_DELETED)
    public void recordDeletedMessage(Member member, Channel channel, String deletedMessage) {

    }

    @Override
    @RequireFeature(GuildFeature.RECORD_EDITED)
    public void recordEditedMessage(Member member, Channel channel, String originMessage) {

    }

    @Override
    public void recordMessage(Message message) {
        MessageRq rq = messageMapper.toMessageRq(message);
        try {
            dcLogApiService.receiveMessage(rq);
        } catch (RuntimeException e) {
            log.error("訊息紀錄失敗");
        }
    }

    private void doImpersonate(Webhook webhook, Member member, String message) {
        String name = member.getEffectiveName();
        User user = member.getUser();
        String avatarUrl = user.getAvatarUrl();
        webhook.sendMessage(message)
            .setUsername(name)
            .setAvatarUrl(avatarUrl)
            .queue(success -> log.info("偽裝成功"));
    }
}
