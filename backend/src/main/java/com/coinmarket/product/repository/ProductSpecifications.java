package com.coinmarket.product.repository;

import com.coinmarket.product.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
                predicates.add(buildKeywordPredicate(root, cb, keyword));
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

    /**
     * Case-insensitive fuzzy keyword match across title / description / country /
     * material / denomination.
     *
     * <ul>
     *   <li>Multi-word keywords are split on whitespace and match ANY term (OR),
     *       so "morgan 1893" matches coins whose title, description, country,
     *       material or denomination contains "morgan" or "1893".</li>
     *   <li>Partial-word matching via a leading/trailing "%" means "morg" finds
     *       "Morgan".</li>
     *   <li>Numeric terms additionally match the barcode (partial match), so
     *       "8890" finds barcode "8890661799553".</li>
     * </ul>
     */
    private static Predicate buildKeywordPredicate(
            Root<Product> root, CriteriaBuilder cb, String keyword) {
        List<Predicate> termPredicates = new ArrayList<>();
        for (String rawToken : keyword.trim().split("\\s+")) {
            if (rawToken.isEmpty()) {
                continue;
            }
            String token = rawToken.toLowerCase();
            String pattern = "%" + token + "%";
            List<Predicate> fieldMatches = new ArrayList<>();
            fieldMatches.add(cb.like(cb.lower(root.get("title")), pattern));
            fieldMatches.add(cb.like(cb.lower(root.get("description")), pattern));
            fieldMatches.add(cb.like(cb.lower(root.get("country")), pattern));
            fieldMatches.add(cb.like(cb.lower(root.get("material")), pattern));
            fieldMatches.add(cb.like(cb.lower(root.get("denomination")), pattern));
            if (token.matches("\\d+")) {
                fieldMatches.add(cb.like(cb.lower(root.get("barcode")), pattern));
            }
            termPredicates.add(cb.or(fieldMatches.toArray(new Predicate[0])));
        }
        return termPredicates.size() == 1
                ? termPredicates.get(0)
                : cb.or(termPredicates.toArray(new Predicate[0]));
    }
}
