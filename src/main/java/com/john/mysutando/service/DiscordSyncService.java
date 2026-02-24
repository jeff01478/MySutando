package com.john.mysutando.service;

import net.dv8tion.jda.api.entities.Guild;

public interface DiscordSyncService {
    void startHistoricalSync(Guild guild);
}
