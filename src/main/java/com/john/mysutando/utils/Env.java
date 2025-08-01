package com.john.mysutando.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Env {

    private static final Properties properties = new Properties();

    private Env() {}

    static {
        try(InputStream inputStream = Env.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (Exception e) {
            log.error("環境變數讀取失敗，請確認是否缺少 config.properties 配置檔案");
            log.error("Error message: {}", e.getMessage());
            throw new RuntimeException();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Properties getNewProperties() {
        return (Properties) properties.clone();
    }
}
