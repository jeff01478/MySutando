package com.john.mysutando.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public interface DiscordTextService {
    void impersonateMember(Member member, TextChannel channel, String message);

    void recordDeletedMessage(String deletedMessageId);

    void recordMessage(Message message);
}
