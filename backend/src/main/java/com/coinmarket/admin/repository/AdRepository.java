package com.coinmarket.admin.repository;

import com.coinmarket.admin.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findAllByOrderBySortOrderAsc();
    List<Ad> findByIsActiveTrueOrderBySortOrderAsc();
}
