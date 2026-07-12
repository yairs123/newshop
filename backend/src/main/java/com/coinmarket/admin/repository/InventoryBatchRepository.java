package com.coinmarket.admin.repository;

import com.coinmarket.admin.entity.InventoryBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long>, JpaSpecificationExecutor<InventoryBatch> {
    List<InventoryBatch> findByProductIdOrderByBatchDateDesc(Long productId);
    Page<InventoryBatch> findAllByOrderByBatchDateDesc(Pageable pageable);
    Page<InventoryBatch> findByProductIdInOrderByBatchDateDesc(List<Long> productIds, Pageable pageable);
    Page<InventoryBatch> findBySupplierContainingIgnoreCaseOrInvoiceNoContainingIgnoreCaseOrderByBatchDateDesc(String supplier, String invoiceNo, Pageable pageable);

    // Calculate average purchase price for a product
    default BigDecimal averagePurchasePrice(Long productId) {
        List<InventoryBatch> batches = findByProductIdOrderByBatchDateDesc(productId);
        if (batches.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = batches.stream()
                .map(b -> b.getPurchasePrice().multiply(BigDecimal.valueOf(b.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalQty = batches.stream().mapToLong(InventoryBatch::getQuantity).sum();
        return totalQty > 0 ? total.divide(BigDecimal.valueOf(totalQty), java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    // --- Global finance aggregation ---

    @Query("SELECT COALESCE(SUM(b.purchasePrice * b.quantity), 0) FROM InventoryBatch b WHERE b.batchDate >= :start AND b.batchDate <= :end")
    BigDecimal sumPurchaseCostBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(b) FROM InventoryBatch b WHERE b.batchDate >= :start AND b.batchDate <= :end")
    Long countBatchesBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(value = """
            SELECT TO_CHAR(b.batch_date, 'YYYY-MM') AS month,
                   COALESCE(SUM(b.purchase_price * b.quantity), 0) AS total_cost,
                   COUNT(*) AS batch_count
            FROM coin_admin.inventory_batches b
            WHERE b.batch_date >= :start
              AND b.batch_date <= :end
            GROUP BY TO_CHAR(b.batch_date, 'YYYY-MM')
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> monthlyPurchaseBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
