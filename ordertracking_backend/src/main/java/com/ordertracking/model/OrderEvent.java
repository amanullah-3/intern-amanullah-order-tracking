package com.ordertracking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_events")
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "rider_id", nullable = false, length = 50)
    private String riderId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public OrderEvent() {
        // Default constructor for JPA
        this.createdAt = LocalDateTime.now();
    }

    public OrderEvent(String orderId, String riderId, String status) {
        this(orderId, riderId, status, LocalDateTime.now());
    }

    public OrderEvent(String orderId, String riderId, String status, LocalDateTime eventTimestamp) {
        this.orderId = orderId;
        this.riderId = riderId;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", riderId='" + riderId + '\'' +
                ", status='" + status + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", createdAt=" + createdAt +
                '}';
    }
}