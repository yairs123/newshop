package com.coinmarket.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.coinmarket.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;

    public List<ProductResponse> search(String keyword, int page, int size) {
        try {
            SearchResponse<ProductResponse> response = esClient.search(s -> s
                    .index("products")
                    .query(q -> q
                            .multiMatch(t -> t
                                    .fields(List.of("title^3", "description", "ratingNumber^2"))
                                    .query(keyword)))
                    .from(page * size)
                    .size(size),
                    ProductResponse.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Search failed", e);
            throw new RuntimeException("Search failed", e);
        }
    }
}
