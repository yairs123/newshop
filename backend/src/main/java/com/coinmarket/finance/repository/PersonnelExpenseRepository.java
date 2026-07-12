package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.PersonnelExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface PersonnelExpenseRepository extends JpaRepository<PersonnelExpense, Long> {

    Page<PersonnelExpense> findAllByOrderByPayDateDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PersonnelExpense p WHERE p.payDate BETWEEN :start AND :end")
    BigDecimal sumByPayDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
