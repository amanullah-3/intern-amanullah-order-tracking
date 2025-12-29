package com.ordertracking.service;

import com.ordertracking.dto.OrderEventDTO;
import com.ordertracking.model.OrderEvent;
import com.ordertracking.repository.OrderEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderEventRepository orderEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderUpdatesTopic = "order-status-updates";

    // Track recent events to prevent duplicates
    private final Map<String, LocalDateTime> lastEventTimestamps = new HashMap<>();

    public OrderService(OrderEventRepository orderEventRepository,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderEventRepository = orderEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void simulateOrderEvent(OrderEventDTO orderEventDTO) {
        log.info("Processing order event: {} - {} - {}",
                orderEventDTO.getOrderId(),
                orderEventDTO.getRiderId(),
                orderEventDTO.getStatus());

        // Check for duplicate events (same order, same status within 1 second)
        String eventKey = orderEventDTO.getOrderId() + "-" + orderEventDTO.getStatus();
        LocalDateTime lastTimestamp = lastEventTimestamps.get(eventKey);
        LocalDateTime currentTime = LocalDateTime.now();

        if (lastTimestamp != null &&
                lastTimestamp.plusSeconds(1).isAfter(currentTime)) {
            log.warn("Duplicate event detected and skipped: {} - {}",
                    orderEventDTO.getOrderId(), orderEventDTO.getStatus());
            return;
        }

        // Update timestamp
        lastEventTimestamps.put(eventKey, currentTime);

        // Send to Kafka - let Kafka consumer handle database persistence and SSE
        try {
            kafkaTemplate.send(orderUpdatesTopic, orderEventDTO.getOrderId(), orderEventDTO);
            log.debug("Event sent to Kafka: {}", orderEventDTO.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send event to Kafka: {}", e.getMessage());
            // Optionally save directly if Kafka fails
            saveEventDirectly(orderEventDTO);
        }
    }

    private void saveEventDirectly(OrderEventDTO orderEventDTO) {
        LocalDateTime timestamp = orderEventDTO.getEventTimestamp();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        OrderEvent orderEvent = new OrderEvent(
                orderEventDTO.getOrderId(),
                orderEventDTO.getRiderId(),
                orderEventDTO.getStatus(),
                timestamp
        );

        orderEventRepository.save(orderEvent);
        log.info("Event saved directly to DB (Kafka failed): {}", orderEventDTO.getOrderId());
    }
}