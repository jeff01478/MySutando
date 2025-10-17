package com.john.mysutando.service;

import java.util.List;

import com.john.mysutando.dto.rs.GuildInfoRs;

public interface BotService {
    List<GuildInfoRs> getAllGuild();
}
