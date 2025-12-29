package com.ordertracking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.dto.OrderEventDTO;
import com.ordertracking.dto.SSEEventDTO;
import com.ordertracking.model.OrderEvent;
import com.ordertracking.repository.OrderEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final OrderEventRepository orderEventRepository;
    private final SSEService sseService;
    private final ObjectMapper objectMapper;
    private final AggregationService aggregationService;

    // Deduplication cache (last 5 seconds)
    private final ConcurrentHashMap<String, Long> processedEvents = new ConcurrentHashMap<>();

    public KafkaConsumerService(OrderEventRepository orderEventRepository,
                                SSEService sseService,
                                ObjectMapper objectMapper,
                                AggregationService aggregationService) {
        this.orderEventRepository = orderEventRepository;
        this.sseService = sseService;
        this.objectMapper = objectMapper;
        this.aggregationService = aggregationService;
    }

    @KafkaListener(topics = "${order.kafka.topic.order-updates}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeOrderEvent(OrderEventDTO orderEventDTO) {
        try {
            // Ensure timestamp is set to current local time
            if (orderEventDTO.getEventTimestamp() == null) {
                orderEventDTO.setEventTimestamp(LocalDateTime.now());
            }

            // Create a unique key for deduplication
            String eventKey = orderEventDTO.getOrderId() + "-" +
                    orderEventDTO.getStatus() + "-" +
                    orderEventDTO.getEventTimestamp().toString();

            long currentTime = System.currentTimeMillis();

            // Check if we've processed this event recently (within 2 seconds)
            Long lastProcessedTime = processedEvents.get(eventKey);
            if (lastProcessedTime != null &&
                    (currentTime - lastProcessedTime) < 2000) {
                log.debug("Duplicate Kafka event skipped: {}", eventKey);
                return;
            }

            // Store in deduplication cache
            processedEvents.put(eventKey, currentTime);

            // Clean old entries from cache (older than 10 seconds)
            processedEvents.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue()) > 10000);

            log.info("Processing Kafka order event: {}", orderEventDTO);

            // Save to database
            OrderEvent orderEvent = new OrderEvent(
                    orderEventDTO.getOrderId(),
                    orderEventDTO.getRiderId(),
                    orderEventDTO.getStatus(),
                    orderEventDTO.getEventTimestamp()
            );

            OrderEvent savedEvent = orderEventRepository.save(orderEvent);
            log.info("Saved order event with ID: {}", savedEvent.getId());

            // Create SSE event using the SAME timestamp from the saved event
            SSEEventDTO sseEvent = new SSEEventDTO(
                    savedEvent.getOrderId(),
                    savedEvent.getRiderId(),
                    savedEvent.getStatus(),
                    savedEvent.getEventTimestamp()  // Use the same timestamp from DB
            );

            // Send SSE update
            sseService.sendEvent(sseEvent);

            // Auto-update today's summary for DELIVERED orders
            if ("DELIVERED".equalsIgnoreCase(orderEventDTO.getStatus())) {
                try {
                    log.info("Auto-updating today's summary due to DELIVERED order: {}", orderEventDTO.getOrderId());
                    aggregationService.generateDailySummaryForDate(java.time.LocalDate.now());
                } catch (Exception summaryException) {
                    log.warn("Failed to auto-update today's summary: {}", summaryException.getMessage());
                    // Don't fail the main event processing if summary update fails
                }
            }

            log.info("Processed and broadcasted order event for order: {}", orderEventDTO.getOrderId());

        } catch (Exception e) {
            log.error("Error processing Kafka order event: {}", orderEventDTO, e);
        }
    }
}