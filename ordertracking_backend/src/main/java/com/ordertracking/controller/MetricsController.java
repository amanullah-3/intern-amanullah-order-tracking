package com.ordertracking.controller;

import com.ordertracking.model.OrderDailySummary;
import com.ordertracking.repository.OrderDailySummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);
    private final OrderDailySummaryRepository dailySummaryRepository;

    public MetricsController(OrderDailySummaryRepository dailySummaryRepository) {
        this.dailySummaryRepository = dailySummaryRepository;
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<OrderDailySummary>> getDailySummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("Fetching daily summary for date: {}", date);
        List<OrderDailySummary> summaries = dailySummaryRepository.findBySummaryDate(date);
        log.info("Found {} summaries for date: {}", summaries.size(), date);
        
        return ResponseEntity.ok(summaries);
    }
}