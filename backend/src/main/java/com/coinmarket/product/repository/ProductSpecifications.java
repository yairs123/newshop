package com.coinmarket.product.repository;

import com.coinmarket.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    public static Specification<Product> withFilters(
            String keyword, Long categoryId, String ratingCompany,
            String ratingGrade, Double minPrice, Double maxPrice,
            String country, Integer year) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), "ACTIVE"));

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("country")), pattern),
                    cb.like(cb.lower(root.get("material")), pattern),
                    cb.like(cb.lower(root.get("denomination")), pattern)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (StringUtils.hasText(ratingCompany)) {
                predicates.add(cb.equal(root.get("ratingCompany"), ratingCompany));
            }
            if (StringUtils.hasText(ratingGrade)) {
                predicates.add(cb.equal(root.get("ratingGrade"), ratingGrade));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (StringUtils.hasText(country)) {
                predicates.add(cb.equal(root.get("country"), country));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
