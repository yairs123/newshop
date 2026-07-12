package com.coinmarket.admin.controller;

import com.coinmarket.admin.dto.BarcodeCodeRequest;
import com.coinmarket.admin.dto.BarcodeCodeResponse;
import com.coinmarket.admin.service.BarcodeCodeService;
import com.coinmarket.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/barcode-codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "条码管理", description = "管理员条码编码管理接口，包括条码类型的增删改查和搜索")
public class BarcodeCodeController {

    private final BarcodeCodeService barcodeCodeService;

    @GetMapping("/type/{codeType}")
    @Operation(summary = "根据类型查询", description = "根据条码编码类型查询编码列表")
    public ApiResponse<List<BarcodeCodeResponse>> getByType(@PathVariable String codeType) {
        return ApiResponse.success(barcodeCodeService.getByType(codeType));
    }

    @GetMapping("/type/{codeType}/active")
    @Operation(summary = "查询活跃编码", description = "根据条码编码类型查询当前活跃的编码列表")
    public ApiResponse<List<BarcodeCodeResponse>> getActiveByType(@PathVariable String codeType) {
        return ApiResponse.success(barcodeCodeService.getActiveByType(codeType));
    }

    @GetMapping("/parent")
    @Operation(summary = "根据父级查询", description = "根据父级类型和值查询子级条码编码")
    public ApiResponse<List<BarcodeCodeResponse>> getByParent(
            @RequestParam String parentType,
            @RequestParam String parentValue) {
        return ApiResponse.success(barcodeCodeService.getByParent(parentType, parentValue));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索条码编码", description = "根据关键字搜索条码编码")
    public ApiResponse<List<BarcodeCodeResponse>> search(@RequestParam String q) {
        return ApiResponse.success(barcodeCodeService.search(q));
    }

    @PostMapping
    @Operation(summary = "创建条码编码", description = "创建新的条码编码")
    public ApiResponse<BarcodeCodeResponse> create(@RequestBody BarcodeCodeRequest request) {
        return ApiResponse.success(barcodeCodeService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新条码编码", description = "更新条码编码信息")
    public ApiResponse<BarcodeCodeResponse> update(@PathVariable Long id, @RequestBody BarcodeCodeRequest request) {
        return ApiResponse.success(barcodeCodeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除条码编码", description = "删除指定条码编码")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        barcodeCodeService.delete(id);
        return ApiResponse.success(null);
    }
}
