package com.coinmarket.order.repository;

import com.coinmarket.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i WHERE o.buyerId = :buyerId AND i.productId = :productId AND o.status = :status")
    boolean existsByBuyerIdAndItemsProductIdAndStatus(@Param("buyerId") Long buyerId, @Param("productId") Long productId, @Param("status") String status);
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<Order> findByBuyerIdAndStatusInOrderByCreatedAtDesc(Long buyerId, List<String> statuses);
    long countBySellerIdAndStatus(Long sellerId, String status);
    List<Order> findTop5BySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status = 'COMPLETED' AND o.completedAt >= :since")
    BigDecimal sumCompletedSalesSince(@Param("sellerId") Long sellerId, @Param("since") LocalDateTime since);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount), 0) FROM coin_order.orders o WHERE o.seller_id = :sellerId AND o.status = 'COMPLETED' AND o.completed_at >= :from AND o.completed_at < :to", nativeQuery = true)
    BigDecimal sumSalesBySellerBetween(@Param("sellerId") Long sellerId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT DISTINCT oi.productId, oi.productTitle, oi.unitPrice, oi.order.id " +
           "FROM OrderItem oi WHERE oi.order.buyerId = :buyerId AND oi.order.status = 'COMPLETED'")
    List<Object[]> findDistinctProductsByBuyerId(@Param("buyerId") Long buyerId);

    // --- Global finance aggregation (not per-seller) ---

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.completedAt >= :start AND o.completedAt <= :end")
    BigDecimal sumCompletedSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED' AND o.completedAt >= :start AND o.completedAt <= :end")
    Long countCompletedOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT TO_CHAR(o.completed_at, 'YYYY-MM') AS month,
                   COALESCE(SUM(o.total_amount), 0) AS revenue,
                   COUNT(*) AS order_count
            FROM coin_order.orders o
            WHERE o.status = 'COMPLETED'
              AND o.completed_at >= :start
              AND o.completed_at <= :end
            GROUP BY TO_CHAR(o.completed_at, 'YYYY-MM')
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> monthlySalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
