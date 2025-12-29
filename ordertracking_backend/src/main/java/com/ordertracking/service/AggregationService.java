package com.ordertracking.service;

import com.ordertracking.model.OrderDailySummary;
import com.ordertracking.model.OrderEvent;
import com.ordertracking.repository.OrderDailySummaryRepository;
import com.ordertracking.repository.OrderEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);

    private final OrderEventRepository orderEventRepository;
    private final OrderDailySummaryRepository dailySummaryRepository;

    public AggregationService(OrderEventRepository orderEventRepository,
                              OrderDailySummaryRepository dailySummaryRepository) {
        this.orderEventRepository = orderEventRepository;
        this.dailySummaryRepository = dailySummaryRepository;
    }

    @Scheduled(cron = "0 0 23 * * *") // Daily at 23:00
    @Transactional
    public void generateDailySummary() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        generateDailySummaryForDate(yesterday);
    }

    public void generateDailySummaryForDate(LocalDate date) {
        log.info("Generating daily summary for date: {}", date);

        try {
            // Get all events for the date
            List<OrderEvent> events = orderEventRepository.findByDate(date);
            log.info("Found {} events for date: {}", events.size(), date);

            if (events.isEmpty()) {
                log.info("No events found for date: {}, creating empty summaries", date);
                createEmptySummariesIfNotExist(date);
                return;
            }

            // Group by rider
            Map<String, List<OrderEvent>> eventsByRider = new HashMap<>();
            for (OrderEvent event : events) {
                eventsByRider.computeIfAbsent(event.getRiderId(), k -> new ArrayList<>()).add(event);
            }

            log.info("Processing events for {} riders", eventsByRider.size());

            // Generate summary for each rider
            for (Map.Entry<String, List<OrderEvent>> entry : eventsByRider.entrySet()) {
                String riderId = entry.getKey();
                List<OrderEvent> riderEvents = entry.getValue();

                log.debug("Processing {} events for rider: {}", riderEvents.size(), riderId);

                OrderDailySummary summary = calculateRealisticRiderSummary(riderId, date, riderEvents);

                // Use saveOrUpdate method to handle duplicates
                saveOrUpdateSummary(summary);
            }

            log.info("Daily summary generation completed successfully for date: {}", date);

        } catch (Exception e) {
            log.error("Error generating daily summary for date {}: {}", date, e.getMessage(), e);
            throw new RuntimeException("Failed to generate daily summary for date: " + date, e);
        }
    }

    private void createEmptySummaries(LocalDate date) {
        // Get existing riders from database instead of hardcoding
        List<String> existingRiders = dailySummaryRepository.findAll().stream()
                .map(OrderDailySummary::getRiderId)
                .distinct()
                .toList();
        
        if (existingRiders.isEmpty()) {
            log.info("No existing riders found, skipping empty summary creation for date: {}", date);
            return;
        }

        for (String riderId : existingRiders) {
            OrderDailySummary summary = new OrderDailySummary(
                    riderId, date, 0, null, 0
            );
            dailySummaryRepository.save(summary);
        }
    }

    private void createEmptySummariesIfNotExist(LocalDate date) {
        // Only create empty summaries for recent dates (within last 7 days)
        LocalDate cutoffDate = LocalDate.now().minusDays(7);
        
        if (date.isBefore(cutoffDate)) {
            log.info("Skipping empty summary creation for old date: {} (before cutoff: {})", date, cutoffDate);
            return;
        }
        
        // Get existing riders from database instead of hardcoding
        List<String> existingRiders = dailySummaryRepository.findAll().stream()
                .map(OrderDailySummary::getRiderId)
                .distinct()
                .toList();
        
        if (existingRiders.isEmpty()) {
            log.info("No existing riders found, skipping empty summary creation for date: {}", date);
            return;
        }

        for (String riderId : existingRiders) {
            // Only create if doesn't exist
            Optional<OrderDailySummary> existing = dailySummaryRepository.findByRiderIdAndSummaryDate(riderId, date);
            if (existing.isEmpty()) {
                OrderDailySummary summary = new OrderDailySummary(
                        riderId, date, 0, null, 0
                );
                dailySummaryRepository.save(summary);
                log.info("Created empty summary for rider {}: {}", riderId, summary);
            } else {
                log.debug("Summary already exists for rider {} on date {}", riderId, date);
            }
        }
    }

    @Transactional
    private void saveOrUpdateSummary(OrderDailySummary summary) {
        try {
            // Try to find existing summary
            Optional<OrderDailySummary> existingSummary = 
                    dailySummaryRepository.findByRiderIdAndSummaryDate(
                            summary.getRiderId(), summary.getSummaryDate());

            if (existingSummary.isPresent()) {
                // Update existing
                OrderDailySummary existing = existingSummary.get();
                existing.setDeliveredOrders(summary.getDeliveredOrders());
                existing.setAvgDeliveryTimeMinutes(summary.getAvgDeliveryTimeMinutes());
                existing.setDelayedOrders(summary.getDelayedOrders());
                dailySummaryRepository.save(existing);
                log.info("Updated existing summary for rider {}: {}", summary.getRiderId(), existing);
            } else {
                // Create new
                dailySummaryRepository.save(summary);
                log.info("Created new summary for rider {}: {}", summary.getRiderId(), summary);
            }
        } catch (Exception e) {
            log.error("Error saving/updating summary for rider {} on date {}: {}", 
                    summary.getRiderId(), summary.getSummaryDate(), e.getMessage());
            // If there's a constraint violation, try to update existing record
            try {
                Optional<OrderDailySummary> existingSummary = 
                        dailySummaryRepository.findByRiderIdAndSummaryDate(
                                summary.getRiderId(), summary.getSummaryDate());
                if (existingSummary.isPresent()) {
                    OrderDailySummary existing = existingSummary.get();
                    existing.setDeliveredOrders(summary.getDeliveredOrders());
                    existing.setAvgDeliveryTimeMinutes(summary.getAvgDeliveryTimeMinutes());
                    existing.setDelayedOrders(summary.getDelayedOrders());
                    dailySummaryRepository.save(existing);
                    log.info("Recovered by updating existing summary for rider {}: {}", summary.getRiderId(), existing);
                }
            } catch (Exception retryException) {
                log.error("Failed to recover from constraint violation: {}", retryException.getMessage());
                throw retryException;
            }
        }
    }

    private OrderDailySummary calculateRealisticRiderSummary(String riderId, LocalDate date, List<OrderEvent> events) {
        // Group events by order
        Map<String, List<OrderEvent>> eventsByOrder = new HashMap<>();
        for (OrderEvent event : events) {
            eventsByOrder.computeIfAbsent(event.getOrderId(), k -> new ArrayList<>()).add(event);
        }

        int deliveredOrders = 0;
        int delayedOrders = 0;
        List<Long> deliveryTimes = new ArrayList<>();

        for (Map.Entry<String, List<OrderEvent>> entry : eventsByOrder.entrySet()) {
            String orderId = entry.getKey();
            List<OrderEvent> orderEvents = entry.getValue();

            // Sort by timestamp
            orderEvents.sort(Comparator.comparing(OrderEvent::getEventTimestamp));

            // Find key events - take the LAST occurrence of each status
            OrderEvent pickupEvent = null;
            OrderEvent deliveredEvent = null;

            for (OrderEvent event : orderEvents) {
                if ("PICKED_UP".equals(event.getStatus())) {
                    pickupEvent = event; // Keep updating to get the latest PICKED_UP
                } else if ("DELIVERED".equals(event.getStatus())) {
                    deliveredEvent = event; // Keep updating to get the latest DELIVERED
                }
            }

            // Only count if order was delivered and we have both events
            if (deliveredEvent != null && pickupEvent != null) {
                deliveredOrders++;

                // Calculate delivery time - ensure positive duration
                long minutes = Duration.between(
                        pickupEvent.getEventTimestamp(),
                        deliveredEvent.getEventTimestamp()
                ).toMinutes();

                // Only add positive delivery times (ignore negative or zero times)
                if (minutes > 0) {
                    deliveryTimes.add(minutes);

                    // Check if delayed (more than 30 minutes is considered delayed)
                    if (minutes > 30) {
                        delayedOrders++;
                        log.debug("Order {} delayed: {} minutes", orderId, minutes);
                    }
                } else {
                    log.warn("Invalid delivery time for order {}: {} minutes (pickup: {}, delivered: {})", 
                            orderId, minutes, pickupEvent.getEventTimestamp(), deliveredEvent.getEventTimestamp());
                }
            }
        }

        // Calculate average delivery time
        Integer avgDeliveryTime = null;
        if (!deliveryTimes.isEmpty()) {
            long totalMinutes = 0;
            for (Long minutes : deliveryTimes) {
                totalMinutes += minutes;
            }
            avgDeliveryTime = (int) (totalMinutes / deliveryTimes.size());
        }

        return new OrderDailySummary(riderId, date, deliveredOrders, avgDeliveryTime, delayedOrders);
    }

    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        LocalDate today = LocalDate.now();
        List<OrderDailySummary> todaySummaries = dailySummaryRepository.findBySummaryDate(today);

        int totalDeliveries = todaySummaries.stream()
                .mapToInt(OrderDailySummary::getDeliveredOrders)
                .sum();
        
        int totalDelays = todaySummaries.stream()
                .mapToInt(OrderDailySummary::getDelayedOrders)
                .sum();

        double successRate = totalDeliveries > 0 
                ? ((totalDeliveries - totalDelays) * 100.0) / totalDeliveries 
                : 100.0;

        metrics.put("date", today);
        metrics.put("totalDeliveries", totalDeliveries);
        metrics.put("totalDelays", totalDelays);
        metrics.put("successRate", Math.round(successRate * 100.0) / 100.0);
        metrics.put("riderCount", todaySummaries.size());

        return metrics;
    }
}