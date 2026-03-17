package com.john.mysutando.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public interface DiscordTextService {
    void impersonateMember(Member member, TextChannel channel, String message);

    void recordDeletedMessage(Member member, Channel channel, String deletedMessage);

    void recordEditedMessage(Member member, Channel channel, String originMessage);

    void recordMessage(Message message);
}
