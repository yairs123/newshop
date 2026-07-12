package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.InventoryEntryRequest;
import com.coinmarket.admin.dto.InventoryEntryResponse;
import com.coinmarket.admin.dto.InventoryOutboundRequest;
import com.coinmarket.admin.service.InventoryService;
import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.product.dto.ProductResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "库存管理", description = "管理员库存管理接口，包括入库记录管理、商品上架、条码生成等操作")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/entry")
    @Operation(summary = "创建入库记录", description = "创建新的库存入库记录，生成商品和条码")
    public ApiResponse<ProductResponse> createEntry(
            @Valid @RequestBody InventoryEntryRequest request,
            @CurrentUser UserPrincipal principal) {
        String operatorName = principal.getUsername();
        return ApiResponse.success(inventoryService.createEntry(request, principal.getId(), operatorName));
    }

    @GetMapping("/entries")
    @Operation(summary = "获取入库记录", description = "分页查询入库记录列表")
    public ApiResponse<PageResponse<InventoryEntryResponse>> listEntries(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "batchDate"));
        Page<InventoryEntryResponse> result = inventoryService.listEntries(q, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/entries/by-date")
    @Operation(summary = "按日期查询入库记录", description = "根据日期范围查询入库记录")
    public ApiResponse<List<InventoryEntryResponse>> listByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ApiResponse.success(inventoryService.listByDateRange(dateFrom, dateTo));
    }

    @GetMapping("/for-listing")
    @Operation(summary = "获取可上架商品", description = "查询可用于上架的商品列表")
    public ApiResponse<List<ProductResponse>> listForListing() {
        return ApiResponse.success(inventoryService.listForListing());
    }

    @GetMapping("/search")
    @Operation(summary = "搜索库存", description = "根据关键字搜索库存商品")
    public ApiResponse<PageResponse<ProductResponse>> searchInventory(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(PageResponse.from(inventoryService.searchInventory(q, pageable)));
    }

    @PostMapping("/list-for-sale/{productId}")
    @Operation(summary = "上架商品", description = "将商品上架销售并设置售价")
    public ApiResponse<ProductResponse> listForSale(
            @PathVariable Long productId,
            @RequestParam BigDecimal price,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(inventoryService.listForSale(productId, price, principal.getId(), principal.getUsername()));
    }

    @PostMapping("/outbound/{productId}")
    @Operation(summary = "库存出库", description = "减少库存数量并记录出库操作")
    public ApiResponse<ProductResponse> outbound(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryOutboundRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(inventoryService.stockOut(productId, request.getQuantity(), request.getReason(), principal.getId(), principal.getUsername()));
    }

    @PutMapping("/batch/{batchId}")
    @Operation(summary = "更新批次信息", description = "更新入库批次的采购价、供应商等")
    public ApiResponse<InventoryEntryResponse> updateBatch(
            @PathVariable Long batchId,
            @RequestBody InventoryEntryRequest request) {
        return ApiResponse.success(inventoryService.updateBatch(batchId, request));
    }

    @PostMapping("/generate-barcode")
    @Operation(summary = "生成条码", description = "根据入库信息生成商品条码")
    public ApiResponse<String> generateBarcode(@RequestBody InventoryEntryRequest request) {
        return ApiResponse.success(inventoryService.generateBarcode(request));
    }
}
