package com.ordertracking.repository;

import com.ordertracking.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findByOrderId(String orderId);

    List<OrderEvent> findByRiderId(String riderId);

    List<OrderEvent> findByStatus(String status);

    @Query("SELECT oe FROM OrderEvent oe WHERE oe.orderId = :orderId ORDER BY oe.eventTimestamp DESC")
    List<OrderEvent> findLatestByOrderId(@Param("orderId") String orderId);

    @Query("SELECT oe FROM OrderEvent oe WHERE oe.eventTimestamp >= :since ORDER BY oe.eventTimestamp DESC")
    List<OrderEvent> findRecentEvents(@Param("since") LocalDateTime since);

    @Query("SELECT oe FROM OrderEvent oe WHERE oe.riderId = :riderId AND CAST(oe.eventTimestamp AS date) = :date")
    List<OrderEvent> findByRiderAndDate(@Param("riderId") String riderId, @Param("date") LocalDate date);

    @Query("SELECT oe FROM OrderEvent oe WHERE CAST(oe.eventTimestamp AS date) = :date")
    List<OrderEvent> findByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT * FROM order_events oe WHERE oe.order_id = :orderId AND oe.status = :status ORDER BY oe.event_timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<OrderEvent> findLatestStatus(@Param("orderId") String orderId, @Param("status") String status);

    @Query("SELECT DISTINCT oe.orderId FROM OrderEvent oe WHERE CAST(oe.eventTimestamp AS date) = :date")
    List<String> findDistinctOrderIdsByDate(@Param("date") LocalDate date);
}