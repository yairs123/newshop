package com.coinmarket.seller.repository;

import com.coinmarket.seller.entity.SellerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {
    List<SellerApplication> findByStatus(String status);
    Optional<SellerApplication> findByUserIdAndStatus(Long userId, String status);
    Optional<SellerApplication> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
