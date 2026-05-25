package com.coinmarket.finance.service;

import com.coinmarket.admin.repository.InventoryBatchRepository;
import com.coinmarket.admin.service.AuditService;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.FinanceDashboardResponse;
import com.coinmarket.finance.dto.ProfitReportResponse;
import com.coinmarket.finance.dto.PurchaseReportResponse;
import com.coinmarket.finance.dto.ReimbursementRequest;
import com.coinmarket.finance.dto.ReimbursementResponse;
import com.coinmarket.finance.dto.SalesRevenueResponse;
import com.coinmarket.finance.entity.Reimbursement;
import com.coinmarket.finance.repository.ReimbursementRepository;
import com.coinmarket.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final ReimbursementRepository reimbursementRepository;
    private final AuditService auditService;
    private final OrderRepository orderRepository;
    private final InventoryBatchRepository inventoryBatchRepository;

    @Transactional
    public ReimbursementResponse createReimbursement(ReimbursementRequest request, Long submitterId) {
        Reimbursement reimbursement = Reimbursement.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .category(request.getCategory())
                .description(request.getDescription())
                .receiptUrl(request.getReceiptUrl())
                .status("PENDING")
                .submitterId(submitterId)
                .build();
        reimbursement = reimbursementRepository.save(reimbursement);
        auditService.logCreate("REIMBURSEMENT", reimbursement.getId(), submitterId, "user:" + submitterId);
        return ReimbursementResponse.from(reimbursement);
    }

    @Transactional
    public ReimbursementResponse approve(Long id, Long approverId) {
        Reimbursement r = reimbursementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报销单不存在"));
        if (!"PENDING".equals(r.getStatus())) {
            throw new BusinessException("只能审批待审核的报销单");
        }
        r.setStatus("APPROVED");
        r.setApproverId(approverId);
        r.setApprovedAt(LocalDateTime.now());
        r = reimbursementRepository.save(r);
        auditService.log("REIMBURSEMENT", id, "UPDATE", "status", "PENDING", "APPROVED",
                approverId, "user:" + approverId, "Approved reimbursement");
        return ReimbursementResponse.from(r);
    }

    @Transactional
    public ReimbursementResponse reject(Long id, Long approverId, String reason) {
        Reimbursement r = reimbursementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报销单不存在"));
        if (!"PENDING".equals(r.getStatus())) {
            throw new BusinessException("只能驳回待审核的报销单");
        }
        r.setStatus("REJECTED");
        r.setApproverId(approverId);
        r.setRejectReason(reason);
        r = reimbursementRepository.save(r);
        auditService.log("REIMBURSEMENT", id, "UPDATE", "status", "PENDING", "REJECTED",
                approverId, "user:" + approverId, "Rejected: " + reason);
        return ReimbursementResponse.from(r);
    }

    @Transactional
    public ReimbursementResponse markPaid(Long id, Long approverId) {
        Reimbursement r = reimbursementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报销单不存在"));
        if (!"APPROVED".equals(r.getStatus())) {
            throw new BusinessException("只能支付已批准的报销单");
        }
        r.setStatus("PAID");
        r.setPaidAt(LocalDateTime.now());
        r = reimbursementRepository.save(r);
        auditService.log("REIMBURSEMENT", id, "UPDATE", "status", "APPROVED", "PAID",
                approverId, "user:" + approverId, "Paid reimbursement");
        return ReimbursementResponse.from(r);
    }

    @Transactional
    public void deleteReimbursement(Long id) {
        if (!reimbursementRepository.existsById(id)) {
            throw new BusinessException("报销单不存在");
        }
        reimbursementRepository.deleteById(id);
    }

    public ReimbursementResponse getReimbursement(Long id) {
        return reimbursementRepository.findById(id)
                .map(ReimbursementResponse::from)
                .orElseThrow(() -> new BusinessException("报销单不存在"));
    }

    public Page<ReimbursementResponse> listReimbursements(String status, Long submitterId, Pageable pageable) {
        if (status != null) {
            return reimbursementRepository.findByStatusOrderByCreatedAtDesc(status)
                    .stream()
                    .map(ReimbursementResponse::from)
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                int start = (int) pageable.getOffset();
                                int end = Math.min(start + pageable.getPageSize(), list.size());
                                var subList = list.subList(start, end);
                                return new org.springframework.data.domain.PageImpl<>(subList, pageable, list.size())
                                        .map(r -> ReimbursementResponse.from(
                                                reimbursementRepository.findById(r.getId()).orElseThrow()));
                            }
                    ));
        }
        if (submitterId != null) {
            return reimbursementRepository.findBySubmitterIdOrderByCreatedAtDesc(submitterId, pageable)
                    .map(ReimbursementResponse::from);
        }
        return reimbursementRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ReimbursementResponse::from);
    }

    /** Financial dashboard: aggregate purchase costs, sales revenue, net profit */
    public FinanceDashboardResponse getDashboard() {
        // Default to current year-to-date
        LocalDate now = LocalDate.now();
        LocalDate yearStart = now.with(java.time.temporal.TemporalAdjusters.firstDayOfYear());

        BigDecimal totalRevenue = orderRepository.sumCompletedSalesBetween(
                yearStart.atStartOfDay(), now.plusDays(1).atStartOfDay());
        BigDecimal totalCost = inventoryBatchRepository.sumPurchaseCostBetween(yearStart, now);

        BigDecimal netProfit = totalRevenue.subtract(totalCost);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        long pendingCount = reimbursementRepository.findByStatusOrderByCreatedAtDesc("PENDING").size();
        long paidCount = reimbursementRepository.findByStatusOrderByCreatedAtDesc("PAID").size();

        return FinanceDashboardResponse.builder()
                .totalPurchaseCost(totalCost)
                .totalSalesRevenue(totalRevenue)
                .netProfit(netProfit)
                .profitMargin(profitMargin.setScale(2, RoundingMode.HALF_UP))
                .pendingReimbursements(pendingCount)
                .totalReimbursements(paidCount)
                .build();
    }

    // --- Financial Reports ---

    public SalesRevenueResponse getSalesRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        BigDecimal totalRevenue = orderRepository.sumCompletedSalesBetween(start, end);
        Long orderCount = orderRepository.countCompletedOrdersBetween(start, end);
        List<Object[]> rows = orderRepository.monthlySalesBetween(start, end);

        List<SalesRevenueResponse.MonthlySales> breakdown = rows.stream()
                .map(r -> new SalesRevenueResponse.MonthlySales(
                        (String) r[0],
                        (BigDecimal) r[1],
                        (Long) r[2]))
                .toList();

        return SalesRevenueResponse.of(totalRevenue, orderCount, breakdown);
    }

    public PurchaseReportResponse getPurchaseReport(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalCost = inventoryBatchRepository.sumPurchaseCostBetween(startDate, endDate);
        Long batchCount = inventoryBatchRepository.countBatchesBetween(startDate, endDate);
        List<Object[]> rows = inventoryBatchRepository.monthlyPurchaseBetween(startDate, endDate);

        List<PurchaseReportResponse.MonthlyPurchase> breakdown = rows.stream()
                .map(r -> new PurchaseReportResponse.MonthlyPurchase(
                        (String) r[0],
                        (BigDecimal) r[1],
                        (Long) r[2]))
                .toList();

        return PurchaseReportResponse.of(totalCost, batchCount, breakdown);
    }

    public ProfitReportResponse getProfitReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDT = startDate.atStartOfDay();
        LocalDateTime endDT = endDate.plusDays(1).atStartOfDay();

        BigDecimal totalRevenue = orderRepository.sumCompletedSalesBetween(startDT, endDT);
        BigDecimal totalCost = inventoryBatchRepository.sumPurchaseCostBetween(startDate, endDate);

        List<Object[]> salesRows = orderRepository.monthlySalesBetween(startDT, endDT);
        List<Object[]> purchaseRows = inventoryBatchRepository.monthlyPurchaseBetween(startDate, endDate);

        // Merge months from both sources
        Set<String> allMonths = new TreeSet<>();
        Map<String, BigDecimal> revenueByMonth = new HashMap<>();
        Map<String, BigDecimal> costByMonth = new HashMap<>();

        for (Object[] r : salesRows) {
            String m = (String) r[0];
            allMonths.add(m);
            revenueByMonth.put(m, (BigDecimal) r[1]);
        }
        for (Object[] r : purchaseRows) {
            String m = (String) r[0];
            allMonths.add(m);
            costByMonth.put(m, (BigDecimal) r[1]);
        }

        List<ProfitReportResponse.MonthlyProfit> breakdown = allMonths.stream().map(m -> {
            BigDecimal rev = revenueByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal cost = costByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal profit = rev.subtract(cost);
            BigDecimal margin = rev.compareTo(BigDecimal.ZERO) > 0
                    ? profit.divide(rev, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            return new ProfitReportResponse.MonthlyProfit(m, rev, cost, profit, margin.setScale(2, RoundingMode.HALF_UP));
        }).toList();

        return ProfitReportResponse.of(totalRevenue, totalCost, breakdown);
    }
}
