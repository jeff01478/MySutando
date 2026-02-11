package com.john.mysutando.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.john.mysutando.dto.rs.GuildInfoRs;
import com.john.mysutando.service.BotService;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    private final JDA jda;

    @Override
    public List<GuildInfoRs> getAllGuild() {
        List<GuildInfoRs> result = new ArrayList<>();
        List<Guild> guilds = jda.getMutualGuilds();
        for (Guild guild : guilds) {
            GuildInfoRs rs = new GuildInfoRs();
            rs.setId(guild.getId());
            rs.setName(guild.getName());
            result.add(rs);
        }
        return result;
    }
}
