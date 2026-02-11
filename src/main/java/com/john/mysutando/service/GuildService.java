package com.john.mysutando.service;

import com.john.mysutando.dto.rs.ChannelInfoRs;

public interface GuildService {
    ChannelInfoRs getAllChannel(Long guildId);
}
