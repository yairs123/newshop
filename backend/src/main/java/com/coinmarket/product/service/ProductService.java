package com.coinmarket.product.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Product;
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
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RatingLookupService ratingLookupService;
    private final ProductIndexService productIndexService;
    private final UserRepository userRepository;

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
                .build();
    }
}
