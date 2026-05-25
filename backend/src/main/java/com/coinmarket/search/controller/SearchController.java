package com.coinmarket.search.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.search.service.ProductIndexService;
import com.coinmarket.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final ProductIndexService productIndexService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(searchService.search(q, page, size));
    }

    @PostMapping("/reindex")
    public ApiResponse<Void> reindex() {
        productIndexService.reindexAll();
        return ApiResponse.success(null);
    }
}
