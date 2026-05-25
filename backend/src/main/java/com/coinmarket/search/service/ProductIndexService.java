package com.coinmarket.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexService {

    private final ElasticsearchClient esClient;
    private final ProductRepository productRepository;

    public void reindexAll() {
        List<Product> products = productRepository.findAll();
        var bulk = BulkRequest.of(b -> {
            for (Product p : products) {
                b.operations(op -> op.index(idx -> idx
                        .index("products")
                        .id(String.valueOf(p.getId()))
                        .document(toResponse(p))));
            }
            return b;
        });
        try {
            var response = esClient.bulk(bulk);
            log.info("Reindexed {} products, errors: {}", products.size(),
                    response.errors() ? "yes" : "no");
        } catch (IOException e) {
            log.error("Reindex failed", e);
            throw new RuntimeException("Reindex failed", e);
        }
    }

    public void indexProduct(Product product) {
        try {
            esClient.index(i -> i
                    .index("products")
                    .id(String.valueOf(product.getId()))
                    .document(toResponse(product)));
        } catch (IOException e) {
            log.error("Failed to index product {}", product.getId(), e);
        }
    }

    public void deleteProduct(Long id) {
        try {
            esClient.delete(d -> d.index("products").id(String.valueOf(id)));
        } catch (IOException e) {
            log.error("Failed to delete product {} from index", id, e);
        }
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sellerId(p.getSellerId())
                .title(p.getTitle())
                .description(p.getDescription())
                .price(p.getPrice())
                .currency(p.getCurrency())
                .stock(p.getStock())
                .categoryId(p.getCategoryId())
                .status(p.getStatus())
                .ratingCompany(p.getRatingCompany())
                .ratingNumber(p.getRatingNumber())
                .ratingGrade(p.getRatingGrade())
                .country(p.getCountry())
                .year(p.getYear())
                .material(p.getMaterial())
                .denomination(p.getDenomination())
                .viewCount(p.getViewCount())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
