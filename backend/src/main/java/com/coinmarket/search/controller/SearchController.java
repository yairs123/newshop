package com.coinmarket.search.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.search.service.ProductIndexService;
import com.coinmarket.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "搜索管理", description = "商品搜索和索引管理接口")
public class SearchController {

    private final SearchService searchService;
    private final ProductIndexService productIndexService;

    @GetMapping
    @Operation(summary = "搜索商品", description = "根据关键词搜索商品")
    public ApiResponse<List<ProductResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(searchService.search(q, page, size));
    }

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重建索引", description = "重新构建商品搜索索引")
    public ApiResponse<Void> reindex() {
        productIndexService.reindexAll();
        return ApiResponse.success(null);
    }
}
