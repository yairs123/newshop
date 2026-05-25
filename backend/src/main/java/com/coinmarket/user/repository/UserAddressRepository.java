package com.coinmarket.user.repository;

import com.coinmarket.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    List<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);
}
