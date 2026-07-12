package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.OtherExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OtherExpenseRepository extends JpaRepository<OtherExpense, Long> {

    Page<OtherExpense> findByCategoryIdOrderByExpenseDateDesc(Long categoryId, Pageable pageable);

    Page<OtherExpense> findAllByOrderByExpenseDateDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM OtherExpense o WHERE o.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT o.categoryId, COALESCE(SUM(o.amount), 0) FROM OtherExpense o " +
           "WHERE o.expenseDate BETWEEN :start AND :end GROUP BY o.categoryId")
    List<Object[]> sumGroupByCategory(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
