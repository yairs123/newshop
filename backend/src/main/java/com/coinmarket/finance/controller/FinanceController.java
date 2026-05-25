package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.FinanceDashboardResponse;
import com.coinmarket.finance.dto.ProfitReportResponse;
import com.coinmarket.finance.dto.PurchaseReportResponse;
import com.coinmarket.finance.dto.ReimbursementRequest;
import com.coinmarket.finance.dto.ReimbursementResponse;
import com.coinmarket.finance.dto.SalesRevenueResponse;
import com.coinmarket.finance.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/dashboard")
    public ApiResponse<FinanceDashboardResponse> getDashboard() {
        return ApiResponse.success(financeService.getDashboard());
    }

    // --- Reimbursements ---

    @GetMapping("/reimbursements")
    public ApiResponse<PageResponse<ReimbursementResponse>> listReimbursements(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long submitterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReimbursementResponse> result = financeService.listReimbursements(status, submitterId, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/reimbursements/{id}")
    public ApiResponse<ReimbursementResponse> getReimbursement(@PathVariable Long id) {
        return ApiResponse.success(financeService.getReimbursement(id));
    }

    @PostMapping("/reimbursements")
    public ApiResponse<ReimbursementResponse> createReimbursement(
            @Valid @RequestBody ReimbursementRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.createReimbursement(request, principal.getId()));
    }

    @PostMapping("/reimbursements/{id}/approve")
    public ApiResponse<ReimbursementResponse> approveReimbursement(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.approve(id, principal.getId()));
    }

    @PostMapping("/reimbursements/{id}/reject")
    public ApiResponse<ReimbursementResponse> rejectReimbursement(
            @PathVariable Long id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.reject(id, principal.getId(), reason));
    }

    @PostMapping("/reimbursements/{id}/pay")
    public ApiResponse<ReimbursementResponse> markPaid(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.markPaid(id, principal.getId()));
    }

    @DeleteMapping("/reimbursements/{id}")
    public ApiResponse<Void> deleteReimbursement(@PathVariable Long id) {
        financeService.deleteReimbursement(id);
        return ApiResponse.success(null);
    }

    // --- Financial Reports ---

    @GetMapping("/sales-revenue")
    public ApiResponse<SalesRevenueResponse> getSalesRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(financeService.getSalesRevenue(startDate, endDate));
    }

    @GetMapping("/purchase-report")
    public ApiResponse<PurchaseReportResponse> getPurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(financeService.getPurchaseReport(startDate, endDate));
    }

    @GetMapping("/profit-report")
    public ApiResponse<ProfitReportResponse> getProfitReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(financeService.getProfitReport(startDate, endDate));
    }
}
