package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.InventoryEntryRequest;
import com.coinmarket.admin.dto.InventoryEntryResponse;
import com.coinmarket.admin.service.InventoryService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/entry")
    public ApiResponse<ProductResponse> createEntry(
            @Valid @RequestBody InventoryEntryRequest request,
            @CurrentUser UserPrincipal principal) {
        String operatorName = principal.getUsername();
        return ApiResponse.success(inventoryService.createEntry(request, principal.getId(), operatorName));
    }

    @GetMapping("/entries")
    public ApiResponse<PageResponse<InventoryEntryResponse>> listEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "batchDate"));
        Page<InventoryEntryResponse> result = inventoryService.listEntries(pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/entries/by-date")
    public ApiResponse<List<InventoryEntryResponse>> listByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ApiResponse.success(inventoryService.listByDateRange(dateFrom, dateTo));
    }

    @GetMapping("/for-listing")
    public ApiResponse<List<ProductResponse>> listForListing() {
        return ApiResponse.success(inventoryService.listForListing());
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> searchInventory(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(PageResponse.from(inventoryService.searchInventory(q, pageable)));
    }

    @PostMapping("/list-for-sale/{productId}")
    public ApiResponse<ProductResponse> listForSale(
            @PathVariable Long productId,
            @RequestParam BigDecimal price,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(inventoryService.listForSale(productId, price, principal.getId(), principal.getUsername()));
    }

    @PostMapping("/generate-barcode")
    public ApiResponse<String> generateBarcode(@RequestBody InventoryEntryRequest request) {
        return ApiResponse.success(inventoryService.generateBarcode(request));
    }
}
