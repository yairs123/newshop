package com.coinmarket.admin.service;

import com.coinmarket.admin.dto.InventoryEntryRequest;
import com.coinmarket.admin.dto.InventoryEntryResponse;
import com.coinmarket.admin.entity.InventoryBatch;
import com.coinmarket.admin.repository.BarcodeCodeRepository;
import com.coinmarket.admin.repository.InventoryBatchRepository;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.entity.ProductImage;
import com.coinmarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;
    private final BarcodeCodeRepository barcodeCodeRepository;
    private final AuditService auditService;

    /**
     * Create an inventory entry: creates/updates product + tracks batch.
     * If a barcode is not provided, auto-generates one from code table values.
     */
    @Transactional
    public ProductResponse createEntry(InventoryEntryRequest request, Long operatorId, String operatorName) {
        String barcode = generateBarcode(request);

        // Check if product with this barcode already exists
        Product product = productRepository.findByBarcode(barcode).orElse(null);

        if (product == null) {
            // Create new product
            product = Product.builder()
                    .sellerId(0L) // admin-owned inventory
                    .title(request.getTitle() != null ? request.getTitle() : barcode)
                    .description(request.getDescription())
                    .price(null)  // no sale price yet
                    .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                    .stock(request.getQuantity() != null ? request.getQuantity() : 1)
                    .status("INVENTORY")
                    .country(resolveLabel(request.getCountryCode()))
                    .year(request.getYear())
                    .material(request.getMaterial())
                    .denomination(resolveLabelWithParent("DENOM", request.getCountryCode(), request.getDenominationCode()))
                    .barcode(barcode)
                    .viewCount(0)
                    .salesCount(0)
                    .purchasePrice(request.getPurchasePrice())
                    .purchaseCurrency(request.getCurrency() != null ? request.getCurrency() : "USD")
                    .supplier(request.getSupplier())
                    .sourceInvoice(request.getInvoiceNo())
                    .saleQty(0)
                    .build();

            // Set rating info if provided
            if (request.getRatingCompany() != null) {
                product.setRatingCompany(request.getRatingCompany());
                product.setRatingNumber(request.getRatingNumber());
                product.setRatingGrade(request.getRatingGrade());
            }

            product = productRepository.save(product);
            auditService.logCreate("PRODUCT", product.getId(), operatorId, operatorName);
        } else {
            // Update existing inventory — increment stock
            int qty = request.getQuantity() != null ? request.getQuantity() : 1;
            product.setStock(product.getStock() + qty);
            if (request.getPurchasePrice() != null) product.setPurchasePrice(request.getPurchasePrice());
            if (request.getSupplier() != null) product.setSupplier(request.getSupplier());
            if (request.getInvoiceNo() != null) product.setSourceInvoice(request.getInvoiceNo());
            productRepository.save(product);
        }

        // Create inventory batch record
        InventoryBatch batch = InventoryBatch.builder()
                .productId(product.getId())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .purchasePrice(request.getPurchasePrice() != null ? request.getPurchasePrice() : java.math.BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .supplier(request.getSupplier())
                .invoiceNo(request.getInvoiceNo())
                .batchDate(request.getBatchDate() != null ? request.getBatchDate() : java.time.LocalDate.now())
                .receiptImage(request.getReceiptImage())
                .operatorId(operatorId)
                .build();
        inventoryBatchRepository.save(batch);

        auditService.logCreate("INVENTORY_BATCH", batch.getId(), operatorId, operatorName);
        return toProductResponse(product);
    }

    /**
     * Generate a 13-digit barcode from code table selections.
     * Format: [3 country][1 category][3 denom][3 era/year][2 grade][1 Luhn]
     */
    public String generateBarcode(InventoryEntryRequest request) {
        String country = padValue(request.getCountryCode(), 3);
        String category = padValue(request.getCategoryCode(), 1);
        String denom = padValue(request.getDenominationCode(), 3);
        String eraYear;
        if (request.getEraCode() != null && !request.getEraCode().isBlank()) {
            eraYear = padValue(request.getEraCode(), 3);
        } else if (request.getYear() != null) {
            eraYear = String.format("%03d", request.getYear() % 1000);
        } else {
            eraYear = "000";
        }
        String grade = padValue(request.getGradeCode(), 2);

        String body = country + category + denom + eraYear + grade;
        return body + luhnCheckDigit(body);
    }

    public Page<InventoryEntryResponse> listEntries(Pageable pageable) {
        Page<InventoryBatch> batches = inventoryBatchRepository.findAllByOrderByBatchDateDesc(pageable);
        return batches.map(this::toInventoryEntryResponse);
    }

    public List<InventoryEntryResponse> listByDateRange(java.time.LocalDate dateFrom, java.time.LocalDate dateTo) {
        // Use product date range query
        Pageable all = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return inventoryBatchRepository.findAllByOrderByBatchDateDesc(all)
                .stream()
                .filter(b -> {
                    if (dateFrom != null && b.getBatchDate().isBefore(dateFrom)) return false;
                    if (dateTo != null && b.getBatchDate().isAfter(dateTo)) return false;
                    return true;
                })
                .map(this::toInventoryEntryResponse)
                .toList();
    }

    public List<ProductResponse> listForListing() {
        return productRepository.findByStatus("INVENTORY")
                .stream().map(this::toProductResponse).toList();
    }

    /** List inventory products for listing with keyword search */
    public Page<ProductResponse> searchInventory(String keyword, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.and(
                    cb.equal(root.get("status"), "INVENTORY"),
                    cb.or(
                            cb.like(cb.lower(root.get("title")), pattern),
                            cb.like(root.get("barcode"), pattern),
                            cb.like(cb.lower(root.get("country")), pattern)
                    )
            );
        };
        return productRepository.findAll(spec, pageable).map(this::toProductResponse);
    }

    /** Move a product from INVENTORY to ACTIVE (list for sale) */
    @Transactional
    public ProductResponse listForSale(Long productId, java.math.BigDecimal price, Long operatorId, String operatorName) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!"INVENTORY".equals(product.getStatus())) {
            throw new BusinessException("该商品不在库存状态");
        }
        product.setPrice(price);
        product.setStatus("ACTIVE");
        product = productRepository.save(product);
        auditService.log("PRODUCT", productId, "STATUS_CHANGE", "status", "INVENTORY", "ACTIVE",
                operatorId, operatorName, "Listed for sale at " + price);
        return toProductResponse(product);
    }

    private String resolveLabel(String codeValue) {
        if (codeValue == null) return null;
        // Try to find by code type; we accept multiple types
        var results = barcodeCodeRepository.findByCodeTypeAndIsActiveTrueOrderBySortOrder("COUNTRY");
        return results.stream()
                .filter(c -> codeValue.equals(c.getCodeValue()))
                .findFirst()
                .map(c -> c.getLabelEn() != null ? c.getLabelEn() : c.getLabelZh())
                .orElse(codeValue);
    }

    private String resolveLabelWithParent(String codeType, String parentValue, String codeValue) {
        if (codeValue == null || parentValue == null) return codeValue;
        var results = barcodeCodeRepository.findByParentTypeAndParentValueOrderBySortOrder(codeType, parentValue);
        return results.stream()
                .filter(c -> codeValue.equals(c.getCodeValue()))
                .findFirst()
                .map(c -> c.getLabelEn() != null ? c.getLabelEn() : c.getLabelZh())
                .orElse(codeValue);
    }

    private String padValue(String value, int length) {
        if (value == null) return "0".repeat(length);
        if (value.length() >= length) return value.substring(0, length);
        return "0".repeat(length - value.length()) + value;
    }

    private char luhnCheckDigit(String digits) {
        int sum = 0;
        boolean alternate = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        int check = (10 - (sum % 10)) % 10;
        return (char) ('0' + check);
    }

    private InventoryEntryResponse toInventoryEntryResponse(InventoryBatch batch) {
        var product = productRepository.findById(batch.getProductId());
        return InventoryEntryResponse.builder()
                .id(batch.getId())
                .productId(batch.getProductId())
                .barcode(product.map(Product::getBarcode).orElse(null))
                .title(product.map(Product::getTitle).orElse(null))
                .country(product.map(Product::getCountry).orElse(null))
                .quantity(batch.getQuantity())
                .purchasePrice(batch.getPurchasePrice())
                .currency(batch.getCurrency())
                .supplier(batch.getSupplier())
                .invoiceNo(batch.getInvoiceNo())
                .batchDate(batch.getBatchDate())
                .receiptImage(batch.getReceiptImage())
                .createdAt(batch.getCreatedAt())
                .build();
    }

    private ProductResponse toProductResponse(Product product) {
        List<String> imageUrls = product.getImages() != null
                ? product.getImages().stream().map(ProductImage::getUrl).toList()
                : new ArrayList<>();
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
                .images(imageUrls)
                .build();
    }
}
