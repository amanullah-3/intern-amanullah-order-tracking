package com.ordertracking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.dto.SSEEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SSEService {

    private static final Logger log = LoggerFactory.getLogger(SSEService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    private final AtomicInteger connectionCounter = new AtomicInteger(0);

    public SSEService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Configure ObjectMapper to use ISO format
        objectMapper.findAndRegisterModules();
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(3600000L); // 1 hour timeout

        int connectionId = connectionCounter.incrementAndGet();

        emitter.onCompletion(() -> {
            log.info("SSE connection {} completed", connectionId);
            removeEmitter(emitter);
        });

        emitter.onTimeout(() -> {
            log.info("SSE connection {} timed out", connectionId);
            sendReconnectEvent(emitter, connectionId);
            removeEmitter(emitter);
        });

        emitter.onError((ex) -> {
            log.error("SSE connection {} error: {}", connectionId, ex.getMessage());
            removeEmitter(emitter);
        });

        emitters.add(emitter);
        log.info("New SSE connection created. ID: {}, Total connections: {}", connectionId, emitters.size());

        return emitter;
    }

    private void sendReconnectEvent(SseEmitter emitter, int connectionId) {
        try {
            String reconnectMessage = String.format(
                    "{\"type\":\"reconnect\",\"message\":\"Connection timed out, please refresh\",\"connectionId\":%d,\"timestamp\":\"%s\"}",
                    connectionId, LocalDateTime.now()
            );

            emitter.send(SseEmitter.event()
                    .id("timeout-" + connectionId)
                    .name("SYSTEM")
                    .data(reconnectMessage, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            log.debug("Error sending reconnect message", e);
        }
    }

    public void sendEvent(SSEEventDTO event) {
        if (emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                String eventJson = objectMapper.writeValueAsString(event);

                emitter.send(SseEmitter.event()
                        .id(event.getOrderId() + "-" + System.currentTimeMillis())
                        .name("ORDER_UPDATE")
                        .data(eventJson, MediaType.APPLICATION_JSON));

                log.debug("Sent SSE event to client: {}", event.getOrderId());

            } catch (JsonProcessingException e) {
                log.error("Error serializing event: {}", event, e);
            } catch (IOException e) {
                log.debug("Client disconnected, marking emitter for removal");
                deadEmitters.add(emitter);
            } catch (IllegalStateException e) {
                log.debug("Emitter is in an illegal state, marking for removal");
                deadEmitters.add(emitter);
            }
        }

        // Remove dead emitters
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.debug("Removed {} dead emitters. Active connections: {}", deadEmitters.size(), emitters.size());
        }
    }

    @Scheduled(fixedRate = 30000) // Send heartbeat every 30 seconds
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id("heartbeat-" + System.currentTimeMillis())
                        .name("HEARTBEAT")
                        .data("{\"type\":\"heartbeat\",\"timestamp\":\"" + LocalDateTime.now() + "\"}"));

            } catch (IOException | IllegalStateException e) {
                deadEmitters.add(emitter);
            }
        }

        // Clean up dead emitters
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.debug("Removed {} dead emitters after heartbeat", deadEmitters.size());
        }
    }

    private void removeEmitter(SseEmitter emitter) {
        if (emitter != null) {
            emitters.remove(emitter);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Error completing emitter", e);
            }
        }
    }

    public int getActiveConnections() {
        return emitters.size();
    }
}