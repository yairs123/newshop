package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findByIsActiveTrueOrderBySortOrder();

    boolean existsByName(String name);
}
