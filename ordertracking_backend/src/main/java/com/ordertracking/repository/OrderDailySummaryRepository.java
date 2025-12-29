package com.ordertracking.repository;

import com.ordertracking.model.OrderDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDailySummaryRepository extends JpaRepository<OrderDailySummary, Long> {

    Optional<OrderDailySummary> findByRiderIdAndSummaryDate(String riderId, LocalDate summaryDate);

    List<OrderDailySummary> findByRiderId(String riderId);

    List<OrderDailySummary> findBySummaryDate(LocalDate summaryDate);

    @Query("SELECT ods FROM OrderDailySummary ods WHERE ods.summaryDate = CURRENT_DATE")
    List<OrderDailySummary> findTodaySummaries();

    @Query("SELECT COALESCE(SUM(ods.deliveredOrders), 0) FROM OrderDailySummary ods WHERE ods.summaryDate = CURRENT_DATE")
    Integer getTotalDeliveriesToday();

    @Query("SELECT COALESCE(SUM(ods.delayedOrders), 0) FROM OrderDailySummary ods WHERE ods.summaryDate = CURRENT_DATE")
    Integer getTotalDelaysToday();
}