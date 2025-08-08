package com.john.mysutando;

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
import java.util.Date;

@Log4j2
public class MyAudioReceiveHandler implements AudioReceiveHandler {

    // 只需要一個串流來儲存所有混合後的音訊
    private final ByteArrayOutputStream combinedAudioStream = new ByteArrayOutputStream();
    private boolean isRecording = true;

    @Override
    public boolean canReceiveCombined() {
        return isRecording;
    }

    /**
     * 每個 20ms 音訊週期，JDA 會呼叫此方法，提供混合後的音訊。
     * @param combinedAudio 包含所有說話者混合後音訊的物件。
     */
    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        // 取得混合後的 PCM 音訊資料
        byte[] mixedPcmData = combinedAudio.getAudioData(0.5); // 音量 1.0 代表原始音量

        try {
            combinedAudioStream.write(mixedPcmData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止錄音並將累積的 PCM 資料儲存成一個 WAV 檔案。
     * @param outputFolder 儲存 WAV 檔案的資料夾路徑。
     */
    public void stopAndSaveAsWav(String outputFolder) {
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
            e.printStackTrace();
        }
    }

    /**
     * 使用 Java Sound API 將 raw PCM data 寫入 WAV 檔案。
     * @param pcmData 完整的 PCM 音訊資料。
     * @param outputFile 要寫入的 File 物件。
     */
    private void saveAsWav(byte[] pcmData, File outputFile) {
        try {
            // 1. 定義音訊格式，這必須與 JDA 提供的 PCM 格式完全相符
            // sampleRate: 48000.0F
            // sampleSizeInBits: 16
            // channels: 2 (Stereo)
            // signed: true (有符號)
            // bigEndian: false (Little-Endian)
            AudioFormat format = OUTPUT_FORMAT;

            // 2. 建立一個 AudioInputStream
            // 它將原始的 byte[] 包裝成一個可讀取的音訊串流
            AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(pcmData),
                    format,
                    pcmData.length / format.getFrameSize()
            );

            // 3. 使用 AudioSystem 將音訊串流寫入檔案
            // AudioFileFormat.Type.WAVE 指定了輸出格式為 WAV
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);

            log.info("成功儲存 WAV 檔案至: {}", outputFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("儲存 WAV 檔案時發生錯誤: {}", e.getMessage());
            throw new RuntimeException();
        }
    }
}
