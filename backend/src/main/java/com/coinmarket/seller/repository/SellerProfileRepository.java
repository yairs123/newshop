package com.coinmarket.seller.repository;

import com.coinmarket.seller.entity.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
