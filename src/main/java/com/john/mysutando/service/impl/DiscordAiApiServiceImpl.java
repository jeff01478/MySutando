package com.john.mysutando.service.impl;

import com.john.mysutando.service.DiscordAiApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;

@Service
@RequiredArgsConstructor
public class DiscordAiApiServiceImpl implements DiscordAiApiService {}
