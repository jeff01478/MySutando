package com.john.mysutando.repository;

import com.john.mysutando.entity.DiscordGuild;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordGuildRepository extends JpaRepository<DiscordGuild, String> {
}
