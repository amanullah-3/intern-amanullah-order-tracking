package com.ordertracking.controller;

import com.ordertracking.service.SSEService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/stream")
public class SSEController {

    private final SSEService sseService;

    public SSEController(SSEService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value = "/order-updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderUpdates() {
        return sseService.createEmitter();
    }
}