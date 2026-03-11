package com.john.mysutando.service;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public interface DiscordSyncService {
    void syncAllAuthGuildMessage(JDA jda);

    void syncGuildMessageAsync(Guild guild);
}
