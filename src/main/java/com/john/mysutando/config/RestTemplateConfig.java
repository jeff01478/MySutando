package com.john.mysutando.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        Timeout timeout = Timeout.ofSeconds(10000);
        RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(timeout)
            .build();
        CloseableHttpClient client = HttpClientBuilder
            .create()
            .setDefaultRequestConfig(config)
            .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
    }
}
