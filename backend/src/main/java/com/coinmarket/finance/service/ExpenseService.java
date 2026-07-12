package com.coinmarket.finance.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.entity.ExpenseCategory;
import com.coinmarket.finance.entity.OtherExpense;
import com.coinmarket.finance.repository.ExpenseCategoryRepository;
import com.coinmarket.finance.repository.OtherExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final OtherExpenseRepository otherExpenseRepository;

    @Transactional(readOnly = true)
    public List<ExpenseCategoryResponse> listCategories() {
        return expenseCategoryRepository.findByIsActiveTrueOrderBySortOrder().stream()
                .map(ExpenseCategoryResponse::from)
                .toList();
    }

    @Transactional
    public ExpenseCategoryResponse createCategory(ExpenseCategoryRequest request) {
        if (expenseCategoryRepository.existsByName(request.getName())) {
            throw new BusinessException("该费用类别已存在");
        }
        ExpenseCategory category = ExpenseCategory.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor() != null ? request.getColor() : "#6b7280")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(true)
                .build();
        category = expenseCategoryRepository.save(category);
        return ExpenseCategoryResponse.from(category);
    }

    @Transactional
    public ExpenseCategoryResponse updateCategory(Long id, ExpenseCategoryRequest request) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用类别不存在"));
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor() != null ? request.getColor() : "#6b7280");
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category = expenseCategoryRepository.save(category);
        return ExpenseCategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用类别不存在"));
        category.setIsActive(false);
        expenseCategoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<OtherExpenseResponse> listExpenses(Long categoryId, Pageable pageable) {
        Page<OtherExpense> page;
        if (categoryId != null) {
            page = otherExpenseRepository.findByCategoryIdOrderByExpenseDateDesc(categoryId, pageable);
        } else {
            page = otherExpenseRepository.findAllByOrderByExpenseDateDesc(pageable);
        }
        return page.map(e -> {
            String catName = expenseCategoryRepository.findById(e.getCategoryId())
                    .map(ExpenseCategory::getName).orElse("未知");
            return OtherExpenseResponse.from(e).withCategoryName(catName);
        });
    }

    @Transactional
    public OtherExpenseResponse createExpense(OtherExpenseRequest request, Long userId) {
        if (!expenseCategoryRepository.existsById(request.getCategoryId())) {
            throw new BusinessException("费用类别不存在");
        }
        OtherExpense expense = OtherExpense.builder()
                .categoryId(request.getCategoryId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "SGD")
                .expenseDate(request.getExpenseDate())
                .description(request.getDescription())
                .vendor(request.getVendor())
                .paymentMethod(request.getPaymentMethod())
                .bankAccountId(request.getBankAccountId())
                .receiptUrl(request.getReceiptUrl())
                .createdBy(userId)
                .build();
        expense = otherExpenseRepository.save(expense);
        String catName = expenseCategoryRepository.findById(expense.getCategoryId())
                .map(ExpenseCategory::getName).orElse("未知");
        return OtherExpenseResponse.from(expense).withCategoryName(catName);
    }

    @Transactional
    public OtherExpenseResponse updateExpense(Long id, OtherExpenseRequest request) {
        OtherExpense expense = otherExpenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用记录不存在"));
        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency() != null ? request.getCurrency() : "SGD");
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription());
        expense.setVendor(request.getVendor());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setBankAccountId(request.getBankAccountId());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense = otherExpenseRepository.save(expense);
        String catName = expenseCategoryRepository.findById(expense.getCategoryId())
                .map(ExpenseCategory::getName).orElse("未知");
        return OtherExpenseResponse.from(expense).withCategoryName(catName);
    }

    @Transactional
    public void deleteExpense(Long id) {
        if (!otherExpenseRepository.existsById(id)) {
            throw new BusinessException("费用记录不存在");
        }
        otherExpenseRepository.deleteById(id);
    }
}
