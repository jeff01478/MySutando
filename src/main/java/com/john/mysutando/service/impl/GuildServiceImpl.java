package com.john.mysutando.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.john.mysutando.dto.rs.ChannelInfoRs;
import com.john.mysutando.model.TextChannelInfo;
import com.john.mysutando.model.VoiceChannelInfo;
import com.john.mysutando.service.GuildService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildServiceImpl implements GuildService {

    private final JDA jda;

    @Override
    public ChannelInfoRs getAllChannel(Long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new IllegalArgumentException();
        }
        List<GuildChannel> guildChannels = guild.getChannels();

        List<TextChannelInfo> textChannelInfoList = new ArrayList<>();
        List<VoiceChannelInfo> voiceChannelInfoList = new ArrayList<>();

        for (GuildChannel guildChannel : guildChannels) {
            ChannelType channelType = guildChannel.getType();
            switch (channelType) {
                case TEXT:
                    textChannelInfoList.add(setTextChannelInfo(guildChannel));
                    break;
                case VOICE:
                    voiceChannelInfoList.add(setVoiceChannelInfo(guildChannel));
                    break;
                default:
            }
        }

        ChannelInfoRs rs = new ChannelInfoRs();
        rs.setAllTextChannelInfo(textChannelInfoList);
        rs.setAllVoiceChannelInfo(voiceChannelInfoList);
        return rs;
    }

    private TextChannelInfo setTextChannelInfo(GuildChannel guildChannel) {
        TextChannelInfo textChannelInfo = new TextChannelInfo();
        textChannelInfo.setId(guildChannel.getId());
        textChannelInfo.setName(guildChannel.getName());
        return textChannelInfo;
    }

    private VoiceChannelInfo setVoiceChannelInfo(GuildChannel guildChannel) {
        VoiceChannel voiceChannel = (VoiceChannel) guildChannel;
        VoiceChannelInfo voiceChannelInfo = new VoiceChannelInfo();
        voiceChannelInfo.setId(voiceChannel.getId());
        voiceChannelInfo.setName(voiceChannel.getName());

        List<Member> isInVoiceChannelMembers = voiceChannel.getMembers();
        List<String> members = new ArrayList<>();
        isInVoiceChannelMembers.forEach(member -> {
            String nickName = member.getNickname();
            log.info("nick name: {}", nickName);
            String memberName = nickName != null ? nickName : member.getEffectiveName();
            members.add(memberName);
        });

        voiceChannelInfo.setMembers(members);
        voiceChannelInfo.setMemberCount(members.toArray().length);

        return voiceChannelInfo;
    }
}
