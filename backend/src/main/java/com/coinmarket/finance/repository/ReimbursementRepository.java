package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.Reimbursement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
    List<Reimbursement> findByStatusOrderByCreatedAtDesc(String status);
    Page<Reimbursement> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Reimbursement> findBySubmitterIdOrderByCreatedAtDesc(Long submitterId, Pageable pageable);
}
