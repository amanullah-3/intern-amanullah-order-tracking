package com.ordertracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SSEEventDTO {
    private String orderId;
    private String riderId;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime serverTimestamp;

    private String message;

    // Constructors
    public SSEEventDTO() {
        this.serverTimestamp = LocalDateTime.now();
    }

    public SSEEventDTO(String orderId, String riderId, String status,
                       LocalDateTime eventTimestamp) {
        this.orderId = orderId;
        this.riderId = riderId;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
        this.serverTimestamp = LocalDateTime.now();  // Use current local time
        this.message = generateMessage();
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
        this.message = generateMessage();
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public LocalDateTime getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(LocalDateTime serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public String getMessage() {
        if (message == null) {
            message = generateMessage();
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String generateMessage() {
        if (orderId == null || riderId == null || status == null) {
            return "System notification";
        }

        switch (status) {
            case "PICKED_UP":
                return String.format("Rider %s picked up Order %s", riderId, orderId);
            case "IN_TRANSIT":
                return String.format("Rider %s is delivering Order %s", riderId, orderId);
            case "DELIVERED":
                return String.format("Rider %s delivered Order %s", riderId, orderId);
            default:
                return String.format("Order %s status: %s", orderId, status);
        }
    }

    @Override
    public String toString() {
        return "SSEEventDTO{" +
                "orderId='" + orderId + '\'' +
                ", riderId='" + riderId + '\'' +
                ", status='" + status + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", serverTimestamp=" + serverTimestamp +
                ", message='" + message + '\'' +
                '}';
    }
}