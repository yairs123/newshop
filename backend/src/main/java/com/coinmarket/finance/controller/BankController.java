package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/finance/banks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "银行管理", description = "银行账户管理接口")
public class BankController {

    private final BankService bankService;

    @Operation(summary = "获取银行列表")
    @GetMapping
    public ApiResponse<List<BankAccountResponse>> listAccounts() {
        return ApiResponse.success(bankService.listAccounts());
    }

    @Operation(summary = "获取银行详情")
    @GetMapping("/{id}")
    public ApiResponse<BankAccountResponse> getAccount(@PathVariable Long id) {
        return ApiResponse.success(bankService.getAccount(id));
    }

    @Operation(summary = "新增银行账户")
    @PostMapping
    public ApiResponse<BankAccountResponse> createAccount(@Valid @RequestBody BankAccountRequest request) {
        return ApiResponse.success(bankService.createAccount(request));
    }

    @Operation(summary = "编辑银行账户")
    @PutMapping("/{id}")
    public ApiResponse<BankAccountResponse> updateAccount(@PathVariable Long id, @Valid @RequestBody BankAccountRequest request) {
        return ApiResponse.success(bankService.updateAccount(id, request));
    }

    @Operation(summary = "删除银行账户（软删除）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable Long id) {
        bankService.deleteAccount(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "手动调整余额")
    @PostMapping("/{id}/adjust")
    public ApiResponse<BankAccountResponse> adjustBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String notes) {
        return ApiResponse.success(bankService.adjustBalance(id, amount, notes));
    }

    @Operation(summary = "银行转账")
    @PostMapping("/transfer")
    public ApiResponse<BankTransferResponse> transfer(
            @Valid @RequestBody BankTransferRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(bankService.transfer(request, principal.getId()));
    }

    @Operation(summary = "转账记录列表")
    @GetMapping("/transfers")
    public ApiResponse<PageResponse<BankTransferResponse>> listTransfers(
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BankTransferResponse> result = bankService.listTransfers(accountId, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }
}
