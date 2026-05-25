package com.coinmarket.order.repository;

import com.coinmarket.order.entity.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {
    List<OrderLog> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
