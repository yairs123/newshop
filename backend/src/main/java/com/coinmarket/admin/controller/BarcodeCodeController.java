package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.BarcodeCodeRequest;
import com.coinmarket.admin.dto.BarcodeCodeResponse;
import com.coinmarket.admin.service.BarcodeCodeService;
import com.coinmarket.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/barcode-codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BarcodeCodeController {

    private final BarcodeCodeService barcodeCodeService;

    @GetMapping("/type/{codeType}")
    public ApiResponse<List<BarcodeCodeResponse>> getByType(@PathVariable String codeType) {
        return ApiResponse.success(barcodeCodeService.getByType(codeType));
    }

    @GetMapping("/type/{codeType}/active")
    public ApiResponse<List<BarcodeCodeResponse>> getActiveByType(@PathVariable String codeType) {
        return ApiResponse.success(barcodeCodeService.getActiveByType(codeType));
    }

    @GetMapping("/parent")
    public ApiResponse<List<BarcodeCodeResponse>> getByParent(
            @RequestParam String parentType,
            @RequestParam String parentValue) {
        return ApiResponse.success(barcodeCodeService.getByParent(parentType, parentValue));
    }

    @GetMapping("/search")
    public ApiResponse<List<BarcodeCodeResponse>> search(@RequestParam String q) {
        return ApiResponse.success(barcodeCodeService.search(q));
    }

    @PostMapping
    public ApiResponse<BarcodeCodeResponse> create(@RequestBody BarcodeCodeRequest request) {
        return ApiResponse.success(barcodeCodeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BarcodeCodeResponse> update(@PathVariable Long id, @RequestBody BarcodeCodeRequest request) {
        return ApiResponse.success(barcodeCodeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        barcodeCodeService.delete(id);
        return ApiResponse.success(null);
    }
}
