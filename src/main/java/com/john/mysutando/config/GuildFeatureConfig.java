package com.john.mysutando.config;

import com.john.mysutando.enums.GuildFeature;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "bot.features")
public class GuildFeatureConfig {
    private Map<String, Set<GuildFeature>> guildSettings;

    public boolean hasFeature(String guildId, GuildFeature feature) {
        if (guildSettings == null || !guildSettings.containsKey(guildId)) {
            return false;
        }
        return guildSettings.get(guildId).contains(feature);
    }
}
