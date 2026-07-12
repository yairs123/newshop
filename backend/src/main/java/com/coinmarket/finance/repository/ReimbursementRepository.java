package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.Reimbursement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
    Page<Reimbursement> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<Reimbursement> findByStatusAndSubmitterIdOrderByCreatedAtDesc(String status, Long submitterId, Pageable pageable);
    Page<Reimbursement> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Reimbursement> findBySubmitterIdOrderByCreatedAtDesc(Long submitterId, Pageable pageable);
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Reimbursement r WHERE r.status = 'PAID' AND r.paidAt BETWEEN :start AND :end")
    BigDecimal sumPaidAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
