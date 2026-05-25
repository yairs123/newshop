package com.coinmarket.product.repository;

import com.coinmarket.product.entity.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findBySellerId(Long sellerId);
    long countBySellerId(Long sellerId);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStatus(String status);
    List<Product> findByRatingNumber(String ratingNumber);
    List<Product> findTop6ByCategoryIdAndIdNot(Long categoryId, Long id, Sort sort);
    java.util.Optional<Product> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
}
