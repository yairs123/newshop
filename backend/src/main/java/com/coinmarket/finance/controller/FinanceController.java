package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.FinanceDashboardResponse;
import com.coinmarket.finance.dto.FinanceOverviewResponse;
import com.coinmarket.finance.dto.ProfitReportResponse;
import com.coinmarket.finance.dto.PurchaseReportResponse;
import com.coinmarket.finance.dto.ReimbursementRequest;
import com.coinmarket.finance.dto.ReimbursementResponse;
import com.coinmarket.finance.dto.SalesRevenueResponse;
import com.coinmarket.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "财务管理", description = "财务管理接口，包括仪表盘、报销管理、财务报表等")
public class FinanceController {

    private final FinanceService financeService;

    @Operation(summary = "获取财务总览", description = "获取财务总览图表数据，包括月度趋势、费用构成和银行余额分布")
    @GetMapping("/overview")
    public ApiResponse<FinanceOverviewResponse> getOverview() {
        return ApiResponse.success(financeService.getOverview());
    }

    @Operation(summary = "获取财务仪表盘", description = "获取财务概览数据，包括总采购成本、总销售收入、净利润、利润率、待处理报销数等")
    @GetMapping("/dashboard")
    public ApiResponse<FinanceDashboardResponse> getDashboard() {
        return ApiResponse.success(financeService.getDashboard());
    }

    // --- Reimbursements ---

    @Operation(summary = "分页查询报销记录", description = "按状态和提交人分页查询报销记录，支持筛选和分页")
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

    @Operation(summary = "获取报销详情", description = "根据ID获取单条报销记录的详细信息")
    @GetMapping("/reimbursements/{id}")
    public ApiResponse<ReimbursementResponse> getReimbursement(@PathVariable Long id) {
        return ApiResponse.success(financeService.getReimbursement(id));
    }

    @Operation(summary = "创建报销申请", description = "提交新的报销申请，需要提供标题、金额、类别等信息")
    @PostMapping("/reimbursements")
    public ApiResponse<ReimbursementResponse> createReimbursement(
            @Valid @RequestBody ReimbursementRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.createReimbursement(request, principal.getId()));
    }

    @Operation(summary = "审核通过报销", description = "审批通过指定的报销申请")
    @PostMapping("/reimbursements/{id}/approve")
    public ApiResponse<ReimbursementResponse> approveReimbursement(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.approve(id, principal.getId()));
    }

    @Operation(summary = "驳回报销申请", description = "驳回指定的报销申请，需要提供驳回原因")
    @PostMapping("/reimbursements/{id}/reject")
    public ApiResponse<ReimbursementResponse> rejectReimbursement(
            @PathVariable Long id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.reject(id, principal.getId(), reason));
    }

    @Operation(summary = "标记报销已支付", description = "将指定报销申请标记为已支付状态")
    @PostMapping("/reimbursements/{id}/pay")
    public ApiResponse<ReimbursementResponse> markPaid(
            @PathVariable Long id,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(financeService.markPaid(id, principal.getId()));
    }

    @Operation(summary = "删除报销记录", description = "根据ID删除指定的报销记录")
    @DeleteMapping("/reimbursements/{id}")
    public ApiResponse<Void> deleteReimbursement(@PathVariable Long id) {
        financeService.deleteReimbursement(id);
        return ApiResponse.success(null);
    }

    // --- Financial Reports ---

    @Operation(summary = "获取销售收入报表", description = "根据日期范围获取销售收入统计数据，包括总收入、订单数量、平均订单金额和月度明细")
    @GetMapping("/sales-revenue")
    public ApiResponse<SalesRevenueResponse> getSalesRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(financeService.getSalesRevenue(startDate, endDate));
    }

    @Operation(summary = "获取采购报表", description = "根据日期范围获取采购成本统计数据，包括总采购成本、批次数量和月度明细")
    @GetMapping("/purchase-report")
    public ApiResponse<PurchaseReportResponse> getPurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(financeService.getPurchaseReport(startDate, endDate));
    }

    @Operation(summary = "获取利润报表", description = "根据日期范围获取利润统计数据，包括总收入、总成本、净利润、利润率和月度明细")
    @GetMapping("/profit-report")
    public ApiResponse<ProfitReportResponse> getProfitReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(financeService.getProfitReport(startDate, endDate));
    }
}
