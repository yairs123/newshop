package com.coinmarket.user.repository;

import com.coinmarket.user.entity.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, Long> {

    List<UserPaymentMethod> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserPaymentMethod> findByIdAndUserId(Long id, Long userId);

    List<UserPaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);
}
