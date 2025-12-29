package com.ordertracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class OrderEventDTO {
    private String orderId;
    private String riderId;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    // Constructors
    public OrderEventDTO() {
        this.eventTimestamp = LocalDateTime.now();
    }

    public OrderEventDTO(String orderId, String riderId, String status) {
        this(orderId, riderId, status, LocalDateTime.now());
    }

    public OrderEventDTO(String orderId, String riderId, String status, LocalDateTime eventTimestamp) {
        this.orderId = orderId;
        this.riderId = riderId;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    @Override
    public String toString() {
        return "OrderEventDTO{" +
                "orderId='" + orderId + '\'' +
                ", riderId='" + riderId + '\'' +
                ", status='" + status + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }
}