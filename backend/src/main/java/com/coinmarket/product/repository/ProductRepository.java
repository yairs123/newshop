package com.coinmarket.product.repository;

import com.coinmarket.product.entity.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findBySellerId(Long sellerId);
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    long countBySellerId(Long sellerId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.sellerId = :sellerId AND p.stock <= :threshold AND p.status <> 'INACTIVE'")
    long countLowStockBySellerId(@Param("sellerId") Long sellerId, @Param("threshold") int threshold);

    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStatus(String status);
    List<Product> findByStatusOrderByCreatedAtDesc(String status);
    List<Product> findByRatingNumber(String ratingNumber);
    List<Product> findTop6ByCategoryIdAndIdNot(Long categoryId, Long id, Sort sort);
    java.util.Optional<Product> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
}
