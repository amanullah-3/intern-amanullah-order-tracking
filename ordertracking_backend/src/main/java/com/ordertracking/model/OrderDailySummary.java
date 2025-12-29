package com.ordertracking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "order_daily_summary", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"rider_id", "summary_date"}))
public class OrderDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rider_id", nullable = false, length = 50)
    private String riderId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "delivered_orders", nullable = false)
    private Integer deliveredOrders = 0;

    @Column(name = "avg_delivery_time_minutes")
    private Integer avgDeliveryTimeMinutes;

    @Column(name = "delayed_orders", nullable = false)
    private Integer delayedOrders = 0;

    // Constructors
    public OrderDailySummary() {
        // Default constructor for JPA
    }

    public OrderDailySummary(String riderId, LocalDate summaryDate, Integer deliveredOrders,
                             Integer avgDeliveryTimeMinutes, Integer delayedOrders) {
        this.riderId = riderId;
        this.summaryDate = summaryDate;
        this.deliveredOrders = deliveredOrders != null ? deliveredOrders : 0;
        this.avgDeliveryTimeMinutes = avgDeliveryTimeMinutes;
        this.delayedOrders = delayedOrders != null ? delayedOrders : 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public LocalDate getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(LocalDate summaryDate) {
        this.summaryDate = summaryDate;
    }

    public Integer getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(Integer deliveredOrders) {
        this.deliveredOrders = deliveredOrders != null ? deliveredOrders : 0;
    }

    public Integer getAvgDeliveryTimeMinutes() {
        return avgDeliveryTimeMinutes;
    }

    public void setAvgDeliveryTimeMinutes(Integer avgDeliveryTimeMinutes) {
        this.avgDeliveryTimeMinutes = avgDeliveryTimeMinutes;
    }

    public Integer getDelayedOrders() {
        return delayedOrders;
    }

    public void setDelayedOrders(Integer delayedOrders) {
        this.delayedOrders = delayedOrders != null ? delayedOrders : 0;
    }

    @Override
    public String toString() {
        return "OrderDailySummary{" +
                "id=" + id +
                ", riderId='" + riderId + '\'' +
                ", summaryDate=" + summaryDate +
                ", deliveredOrders=" + deliveredOrders +
                ", avgDeliveryTimeMinutes=" + avgDeliveryTimeMinutes +
                ", delayedOrders=" + delayedOrders +
                '}';
    }
}