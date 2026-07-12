package com.coinmarket.admin.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coinmarket.common.util.BarcodeUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponse> listProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<ProductResponse> listProductsFiltered(String status, Boolean printed, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (printed != null) {
                if (printed) {
                    predicates.add(cb.isNotNull(root.get("printedAt")));
                } else {
                    predicates.add(cb.isNull(root.get("printedAt")));
                }
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public List<ProductResponse> listByDateRange(LocalDate dateFrom, LocalDate dateTo, Boolean printed) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.atTime(LocalTime.MAX)));
            }
            if (printed != null) {
                if (printed) {
                    predicates.add(cb.isNotNull(root.get("printedAt")));
                } else {
                    predicates.add(cb.isNull(root.get("printedAt")));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public ProductResponse createProduct(Long sellerId, ProductCreateRequest req) {
        Product product = Product.builder()
                .sellerId(sellerId)
                .title(req.getTitle())
                .description(req.getDescription())
                .price(req.getPrice())
                .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
                .stock(req.getStock() != null ? req.getStock() : 0)
                .categoryId(req.getCategoryId())
                .status("ACTIVE")
                .ratingCompany(req.getRatingCompany())
                .ratingNumber(req.getRatingNumber())
                .ratingGrade(req.getRatingGrade())
                .country(req.getCountry())
                .year(req.getYear())
                .material(req.getMaterial())
                .denomination(req.getDenomination())
                .weight(req.getWeight())
                .barcode(req.getBarcode() != null ? req.getBarcode() : BarcodeUtil.generate())
                .viewCount(0)
                .salesCount(0)
                .build();
        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductCreateRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getCurrency() != null) product.setCurrency(req.getCurrency());
        if (req.getStock() != null) product.setStock(req.getStock());
        if (req.getCategoryId() != null) product.setCategoryId(req.getCategoryId());
        if (req.getRatingCompany() != null) product.setRatingCompany(req.getRatingCompany());
        if (req.getRatingNumber() != null) product.setRatingNumber(req.getRatingNumber());
        if (req.getRatingGrade() != null) product.setRatingGrade(req.getRatingGrade());
        if (req.getCountry() != null) product.setCountry(req.getCountry());
        if (req.getYear() != null) product.setYear(req.getYear());
        if (req.getMaterial() != null) product.setMaterial(req.getMaterial());
        if (req.getDenomination() != null) product.setDenomination(req.getDenomination());
        if (req.getWeight() != null) product.setWeight(req.getWeight());
        if (req.getPurchasePrice() != null) product.setPurchasePrice(req.getPurchasePrice());
        if (req.getPurchaseCurrency() != null) product.setPurchaseCurrency(req.getPurchaseCurrency());
        if (req.getSupplier() != null) product.setSupplier(req.getSupplier());
        if (req.getSourceInvoice() != null) product.setSourceInvoice(req.getSourceInvoice());
        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public void updateProductStatus(Long id, String status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setStatus(status);
        productRepository.save(product);
    }

    @Transactional
    public void markAsPrinted(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setPrintedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Transactional
    public void markAsPrintedByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BusinessException("商品不存在: " + barcode));
        product.setPrintedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Transactional
    public void markBatchAsPrinted(List<Long> ids) {
        List<Product> products = productRepository.findAllById(ids);
        for (Product p : products) {
            p.setPrintedAt(LocalDateTime.now());
        }
        productRepository.saveAll(products);
    }

    public ProductResponse findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .map(this::toResponse)
                .orElse(null);
    }

    public Page<ProductResponse> searchByKeyword(String keyword, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(root.get("barcode"), pattern),
                    cb.like(cb.lower(root.get("country")), pattern),
                    cb.like(cb.lower(root.get("material")), pattern),
                    cb.like(cb.lower(root.get("denomination")), pattern),
                    cb.like(cb.lower(root.get("ratingGrade")), pattern),
                    cb.like(cb.lower(root.get("ratingNumber")), pattern)
            );
        };
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .stock(product.getStock())
                .categoryId(product.getCategoryId())
                .status(product.getStatus())
                .ratingCompany(product.getRatingCompany())
                .ratingNumber(product.getRatingNumber())
                .ratingGrade(product.getRatingGrade())
                .country(product.getCountry())
                .year(product.getYear())
                .material(product.getMaterial())
                .denomination(product.getDenomination())
                .weight(product.getWeight())
                .barcode(product.getBarcode())
                .viewCount(product.getViewCount())
                .salesCount(product.getSalesCount())
                .printedAt(product.getPrintedAt())
                .createdAt(product.getCreatedAt())
                .purchasePrice(product.getPurchasePrice())
                .purchaseCurrency(product.getPurchaseCurrency())
                .supplier(product.getSupplier())
                .sourceInvoice(product.getSourceInvoice())
                .saleQty(product.getSaleQty())
                .build();
    }
}
