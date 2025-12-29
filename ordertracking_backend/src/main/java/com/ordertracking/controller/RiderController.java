package com.ordertracking.controller;

import com.ordertracking.dto.OrderEventDTO;
import com.ordertracking.dto.RiderEventDTO;
import com.ordertracking.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rider")
public class RiderController {

    private static final Logger log = LoggerFactory.getLogger(RiderController.class);
    
    private final OrderService orderService;

    public RiderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Submit an order status update as a rider
     */
    @PostMapping("/{riderId}/orders/update")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String riderId,
            @Valid @RequestBody RiderEventDTO riderEventDTO) {
        
        log.info("Rider {} updating order {} to status {}", 
                riderId, riderEventDTO.getOrderId(), riderEventDTO.getStatus());

        try {
            // Convert RiderEventDTO to OrderEventDTO
            OrderEventDTO orderEventDTO = new OrderEventDTO(
                    riderEventDTO.getOrderId(),
                    riderId,
                    riderEventDTO.getStatus(),
                    riderEventDTO.getEventTimestamp()
            );

            // Submit the event
            orderService.simulateOrderEvent(orderEventDTO);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order status updated successfully");
            response.put("orderId", riderEventDTO.getOrderId());
            response.put("riderId", riderId);
            response.put("status", riderEventDTO.getStatus());
            response.put("timestamp", LocalDateTime.now());
            
            if (riderEventDTO.getNotes() != null && !riderEventDTO.getNotes().trim().isEmpty()) {
                response.put("notes", riderEventDTO.getNotes());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating order status for rider {}: {}", riderId, e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update order status");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Quick status update - simplified endpoint for mobile apps
     */
    @PostMapping("/{riderId}/orders/{orderId}/status/{status}")
    public ResponseEntity<Map<String, Object>> quickStatusUpdate(
            @PathVariable String riderId,
            @PathVariable String orderId,
            @PathVariable String status) {
        
        log.info("Quick status update: Rider {} updating order {} to {}", riderId, orderId, status);

        // Validate status
        if (!isValidRiderStatus(status)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid status. Allowed: PICKED_UP, IN_TRANSIT, DELIVERED");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            OrderEventDTO orderEventDTO = new OrderEventDTO(orderId, riderId, status);
            orderService.simulateOrderEvent(orderEventDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("orderId", orderId);
            response.put("riderId", riderId);
            response.put("status", status);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in quick status update: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update status");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private boolean isValidRiderStatus(String status) {
        return status != null && (
                "PICKED_UP".equals(status) ||
                "IN_TRANSIT".equals(status) ||
                "DELIVERED".equals(status)
        );
    }
}