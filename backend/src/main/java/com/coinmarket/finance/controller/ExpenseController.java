package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "费用管理", description = "费用类别和费用记录管理接口")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "获取费用类别列表")
    @GetMapping("/expense-categories")
    public ApiResponse<List<ExpenseCategoryResponse>> listCategories() {
        return ApiResponse.success(expenseService.listCategories());
    }

    @Operation(summary = "新增费用类别")
    @PostMapping("/expense-categories")
    public ApiResponse<ExpenseCategoryResponse> createCategory(@Valid @RequestBody ExpenseCategoryRequest request) {
        return ApiResponse.success(expenseService.createCategory(request));
    }

    @Operation(summary = "编辑费用类别")
    @PutMapping("/expense-categories/{id}")
    public ApiResponse<ExpenseCategoryResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody ExpenseCategoryRequest request) {
        return ApiResponse.success(expenseService.updateCategory(id, request));
    }

    @Operation(summary = "删除费用类别（软删除）")
    @DeleteMapping("/expense-categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        expenseService.deleteCategory(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取费用记录列表")
    @GetMapping("/expenses")
    public ApiResponse<PageResponse<OtherExpenseResponse>> listExpenses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        Page<OtherExpenseResponse> result = expenseService.listExpenses(categoryId, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @Operation(summary = "新增费用记录")
    @PostMapping("/expenses")
    public ApiResponse<OtherExpenseResponse> createExpense(
            @Valid @RequestBody OtherExpenseRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(expenseService.createExpense(request, principal.getId()));
    }

    @Operation(summary = "编辑费用记录")
    @PutMapping("/expenses/{id}")
    public ApiResponse<OtherExpenseResponse> updateExpense(
            @PathVariable Long id, @Valid @RequestBody OtherExpenseRequest request) {
        return ApiResponse.success(expenseService.updateExpense(id, request));
    }

    @Operation(summary = "删除费用记录")
    @DeleteMapping("/expenses/{id}")
    public ApiResponse<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ApiResponse.success(null);
    }
}
