package com.coinmarket.product.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Category;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.CategoryRepository;
import com.coinmarket.product.repository.ProductRepository;
import com.coinmarket.product.repository.ProductSpecifications;
import com.coinmarket.search.service.ProductIndexService;
import com.coinmarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coinmarket.common.util.BarcodeUtil;
import com.coinmarket.product.repository.ReviewRepository;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RatingLookupService ratingLookupService;
    private final ProductIndexService productIndexService;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ProductResponse createProduct(Long sellerId, ProductCreateRequest request) {
        Product product = Product.builder()
                .sellerId(sellerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .stock(request.getStock() != null ? request.getStock() : 0)
                .categoryId(request.getCategoryId())
                .ratingCompany(request.getRatingCompany())
                .ratingNumber(request.getRatingNumber())
                .country(request.getCountry())
                .year(request.getYear())
                .material(request.getMaterial())
                .denomination(request.getDenomination())
                .weight(request.getWeight())
                .barcode(request.getBarcode() != null ? request.getBarcode() : BarcodeUtil.generate())
                .purchasePrice(request.getPurchasePrice())
                .purchaseCurrency(request.getPurchaseCurrency())
                .supplier(request.getSupplier())
                .sourceInvoice(request.getSourceInvoice())
                .status("ACTIVE")
                .viewCount(0)
                .salesCount(0)
                .build();

        if (request.getRatingNumber() != null && request.getRatingCompany() != null) {
            var ratingInfo = ratingLookupService.lookup(
                    request.getRatingCompany(), request.getRatingNumber());
            product.setRatingGrade(ratingInfo != null ? ratingInfo.getGrade() : null);
        }

        product = productRepository.save(product);
        productIndexService.indexProduct(product);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String keyword, Long categoryId, String ratingCompany,
            String ratingGrade, Double minPrice, Double maxPrice,
            String country, Integer year, Pageable pageable) {
        var spec = ProductSpecifications.withFilters(
                keyword, categoryId, ratingCompany, ratingGrade,
                minPrice, maxPrice, country, year);
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, Long sellerId, ProductCreateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权修改此商品");
        }
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCurrency(request.getCurrency());
        product.setStock(request.getStock());
        product.setCategoryId(request.getCategoryId());
        product.setCountry(request.getCountry());
        product.setYear(request.getYear());
        product.setMaterial(request.getMaterial());
        product.setDenomination(request.getDenomination());
        product.setWeight(request.getWeight());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setPurchaseCurrency(request.getPurchaseCurrency());
        product.setSupplier(request.getSupplier());
        product.setSourceInvoice(request.getSourceInvoice());
        product = productRepository.save(product);
        productIndexService.indexProduct(product);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (product.getCategoryId() == null) return Collections.emptyList();
        List<Product> related = productRepository
                .findTop6ByCategoryIdAndIdNot(product.getCategoryId(), productId,
                        Sort.by(Sort.Direction.DESC, "createdAt"));
        return related.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void setProductStatus(Long id, String status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setStatus(status);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse copyProduct(Long id, Long sellerId) {
        Product original = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!original.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        Product copy = Product.builder()
                .sellerId(sellerId)
                .title(original.getTitle() + " (副本)")
                .description(original.getDescription())
                .price(original.getPrice())
                .currency(original.getCurrency())
                .stock(original.getStock())
                .categoryId(original.getCategoryId())
                .status("ACTIVE")
                .ratingCompany(original.getRatingCompany())
                .ratingNumber(original.getRatingNumber())
                .ratingGrade(original.getRatingGrade())
                .country(original.getCountry())
                .year(original.getYear())
                .material(original.getMaterial())
                .denomination(original.getDenomination())
                .weight(original.getWeight())
                .barcode(BarcodeUtil.generate())
                .viewCount(0)
                .salesCount(0)
                .purchasePrice(original.getPurchasePrice())
                .purchaseCurrency(original.getPurchaseCurrency())
                .supplier(original.getSupplier())
                .sourceInvoice(original.getSourceInvoice())
                .build();
        copy = productRepository.save(copy);
        productIndexService.indexProduct(copy);
        return toResponse(copy);
    }

    @Transactional
    public void batchUpdateStatus(List<Long> ids, String status, Long sellerId) {
        List<Product> products = productRepository.findAllById(ids);
        for (Product p : products) {
            if (!p.getSellerId().equals(sellerId)) continue;
            p.setStatus(status);
            productRepository.save(p);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listSellerProducts(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse markAsPrinted(Long id, Long sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        product.setPrintedAt(java.time.LocalDateTime.now());
        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public void batchMarkAsPrinted(List<Long> ids, Long sellerId) {
        List<Product> products = productRepository.findAllById(ids);
        for (Product p : products) {
            if (!p.getSellerId().equals(sellerId)) continue;
            p.setPrintedAt(java.time.LocalDateTime.now());
            productRepository.save(p);
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        List<String> imageUrls = product.getImages() != null
                ? product.getImages().stream().map(img -> img.getUrl()).toList()
                : Collections.emptyList();

        String sellerName = userRepository.findById(product.getSellerId())
                .map(u -> u.getDisplayName() != null ? u.getDisplayName() : u.getUsername())
                .orElse(null);

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
                .barcode(product.getBarcode())
                .country(product.getCountry())
                .year(product.getYear())
                .material(product.getMaterial())
                .denomination(product.getDenomination())
                .viewCount(product.getViewCount())
                .images(imageUrls)
                .sellerName(sellerName)
                .createdAt(product.getCreatedAt())
                .purchasePrice(product.getPurchasePrice())
                .purchaseCurrency(product.getPurchaseCurrency())
                .supplier(product.getSupplier())
                .sourceInvoice(product.getSourceInvoice())
                .saleQty(product.getSaleQty())
                .averageRating(reviewRepository.averageRatingByProductId(product.getId()))
                .totalReviews(reviewRepository.countByProductId(product.getId()))
                .build();
    }
}
