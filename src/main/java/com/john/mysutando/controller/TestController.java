package com.john.mysutando.controller;

import com.john.mysutando.event.EventManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class TestController {

    private final EventManager eventManager;

    @GetMapping("/test")
    String getGood() {
        log.info("{}", eventManager.getAllEvent());
        return "GOOD";
    }
}
