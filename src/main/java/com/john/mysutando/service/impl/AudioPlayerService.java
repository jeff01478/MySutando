package com.john.mysutando.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.john.mysutando.AudioPlayerSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.audio.AudioSendHandler;

@Log4j2
@Service
public class AudioPlayerService {

    private final AudioPlayerManager playerManager;
    private final AudioPlayer audioPlayer;
    @Getter
    private final AudioSendHandler audioSendHandler;

    @Value("${domain}")
    private String domain;

    public AudioPlayerService() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.playerManager.registerSourceManager(new HttpAudioSourceManager()); // 支援 HTTP URL
        this.audioPlayer = playerManager.createPlayer();
        this.audioSendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void loadAndPlay(String text) {
        String identifier = domain + "/voice/" + text;
        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioPlayer.playTrack(track);
                log.info("Started playing: {}", track.getInfo().title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (!playlist.getTracks().isEmpty()) {
                    audioPlayer.playTrack(playlist.getTracks().get(0));
                    log.info("Started playing first track from playlist: {}", playlist.getTracks().get(0).getInfo().title);
                }
            }

            @Override
            public void noMatches() {
                log.info("No track found for identifier: {}", identifier);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                log.info("Failed to load track: {}", exception.getMessage());
            }
        });
    }
}
