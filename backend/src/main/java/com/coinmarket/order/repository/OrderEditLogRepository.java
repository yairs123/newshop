package com.coinmarket.order.repository;

import com.coinmarket.order.entity.OrderEditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEditLogRepository extends JpaRepository<OrderEditLog, Long> {
    List<OrderEditLog> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
