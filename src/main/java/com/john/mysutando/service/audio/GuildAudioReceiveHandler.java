package com.john.mysutando.service.audio;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.john.mysutando.dto.rs.VoiceChannelAudioRs;

@Log4j2
@Service
@Scope("prototype")
@RequiredArgsConstructor
public class GuildAudioReceiveHandler implements AudioReceiveHandler {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Getter
    private final Long guildId;

    // 用來儲存所有混合後的音訊
    private final ByteArrayOutputStream combinedAudioStream = new ByteArrayOutputStream();

    private final byte[] emptyArray = new byte[3840];

    private boolean isListen = false;

    private boolean isRecording = false;

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    // 每 20ms 音訊週期，JDA 會呼叫此方法，提供混合後的音訊。
    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        // 取得混合後的 PCM 音訊資料
        byte[] mixedPcmData = combinedAudio.getAudioData(1); // 音量 1.0 代表原始音量

        if (isListen && !Arrays.equals(mixedPcmData, emptyArray)) {
            sendAudioToWebSocket(mixedPcmData);
        }

        if (isRecording) {
            try {
                combinedAudioStream.write(mixedPcmData);
            } catch (IOException e) {
                log.error("寫入音訊時發生 I/O 錯誤", e);
                isRecording = false;
                try {
                    combinedAudioStream.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void startListen() {
        isListen = true;
    }

    public void stopListen() {
        isListen = false;
    }

    public void startRecording() {
        isRecording = true;
    }

    // 停止錄音並將累積的 PCM 資料儲存成一個 WAV 檔案
    public void stopRecordingAndSaveAsWav(String outputFolder) {
        this.isRecording = false;

        log.info("準備將混合音訊儲存成 WAV 檔案...");

        try {
            // 從串流中取得完整的 PCM 音訊資料
            byte[] pcmData = combinedAudioStream.toByteArray();
            combinedAudioStream.close();

            if (pcmData.length == 0) {
                log.info("沒有錄製到任何音訊，不建立檔案。");
                return;
            }

            // 確保輸出的資料夾存在
            Files.createDirectories(Paths.get(outputFolder));

            // 建立一個有時間戳的檔名
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputFile = Paths.get(outputFolder, "recording_" + timestamp + ".wav").toFile();

            // 呼叫 WAV 儲存方法
            saveAsWav(pcmData, outputFile);

        } catch (IOException e) {
            log.error("儲存音訊時發生 I/O 錯誤", e);
        }
    }

    public void closeResource() throws IOException {
        combinedAudioStream.close();
    }

    private void saveAsWav(byte[] pcmData, File outputFile) {
        try {
            AudioFormat format = OUTPUT_FORMAT;

            AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(pcmData),
                    format,
                    pcmData.length / format.getFrameSize()
            );

            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);

            log.info("成功儲存 WAV 檔案至: {}", outputFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("儲存 WAV 檔案時發生錯誤: {}", e.getMessage());
            throw new RuntimeException();
        }
    }

    private byte[] pcmToWav(byte[] pcmData) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        AudioFormat format = OUTPUT_FORMAT;

        AudioInputStream audioInputStream = new AudioInputStream(
            new ByteArrayInputStream(pcmData),
            format,
            pcmData.length / format.getFrameSize()
        );

        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, byteArrayOutputStream);

        audioInputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    private void sendAudioToWebSocket(byte[] pcmData) {
        VoiceChannelAudioRs rs = new VoiceChannelAudioRs();
        byte[] wavData = new byte[0];
        try {
            wavData = pcmToWav(pcmData);
        } catch (IOException e) {
            log.error("音訊轉換發生錯誤", e);
            isListen = false;
        }
        rs.setAudioData(wavData);
        simpMessagingTemplate.convertAndSend("/topic/audio-stream/" + guildId, rs);
    }
}
