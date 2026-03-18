package com.john.mysutando.util;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.mysutando.event.DiscordEmergencyAlertEvent;
import com.john.mysutando.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public <R> R get(String url, HttpHeaders headers, Class<R> responseType) {
        return execute(url, HttpMethod.GET, null, headers, responseType);
    }

    public <T, R> R post(String url, T body, HttpHeaders headers, Class<R> responseType) {
        return execute(url, HttpMethod.POST, body, headers, responseType);
    }

    public <T, R> R put(String url, T body, HttpHeaders headers, Class<R> responseType) {
        return execute(url, HttpMethod.PUT, body, headers, responseType);
    }

    public <T, R> R patch(String url, T body, HttpHeaders headers, Class<R> responseType) {
        return execute(url, HttpMethod.PATCH, body, headers, responseType);
    }

    public <R> R delete(String url, HttpHeaders headers, Class<R> responseType) {
        return execute(url, HttpMethod.DELETE, null, headers, responseType);
    }

    public <T, R> R execute(String url, HttpMethod method, T body, HttpHeaders headers, Class<R> responseType) {
        log.info("start {} api", method.name());
        logRequest(url, body);

        HttpHeaders requestHeaders = (headers != null) ? headers : new HttpHeaders();

        if (requestHeaders.getContentType() == null) {
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<T> entity = new HttpEntity<>(body, headers);

        try {
            long startTime = System.currentTimeMillis();

            ResponseEntity<R> response = restTemplate.exchange(
                url,
                method,
                entity,
                responseType
            );

            log.info("API 請求成功: {} (耗時: {}ms)", url, System.currentTimeMillis() - startTime);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String errorMsg = String.format("API 請求失敗 [%s]: %s, Body: %s",
                url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(errorMsg, e);
        } catch (RestClientException e) {
            String errorMsg = String.format("API 網路連線異常 [%s]", url);
            throw new ApiException(errorMsg, e);
        }
    }

    private void logRequest(String url, Object body) {
        String rqName = "empty";
        String jsonBody = "{}";
        try {
            if (body != null) {
                rqName = body.getClass().getSimpleName();
                jsonBody = objectMapper.writeValueAsString(body);
            }
            log.info("send url: {}, rq: {} Body: {}", url, rqName, jsonBody);
        } catch (JsonProcessingException e) {
            log.warn("日誌 JSON 序列化失敗: {}", e.getMessage());
        }
    }
}
