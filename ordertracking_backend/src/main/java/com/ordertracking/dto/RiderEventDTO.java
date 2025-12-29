package com.ordertracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class RiderEventDTO {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PICKED_UP|IN_TRANSIT|DELIVERED", 
             message = "Status must be one of: PICKED_UP, IN_TRANSIT, DELIVERED")
    private String status;
    
    private String notes;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    // Constructors
    public RiderEventDTO() {
        this.eventTimestamp = LocalDateTime.now();
    }

    public RiderEventDTO(String orderId, String status) {
        this(orderId, status, null, LocalDateTime.now());
    }

    public RiderEventDTO(String orderId, String status, String notes, LocalDateTime eventTimestamp) {
        this.orderId = orderId;
        this.status = status;
        this.notes = notes;
        this.eventTimestamp = eventTimestamp;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    @Override
    public String toString() {
        return "RiderEventDTO{" +
                "orderId='" + orderId + '\'' +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }
}