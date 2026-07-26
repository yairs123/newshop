package com.coinmarket.common.storage;

import com.coinmarket.product.entity.ProductImage;
import com.coinmarket.product.repository.ProductImageRepository;
import com.coinmarket.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Abstract base for FileStorageService implementations that also persist
 * ProductImage database records when storing product images.
 * <p>
 * Subclasses only need to implement the pure file-storage operations
 * ({@link #store(String, String, byte[])} etc.), while the combined
 * product-image flow is handled here.
 */
@RequiredArgsConstructor
public abstract class AbstractFileStorageService implements FileStorageService {

    protected final ProductRepository productRepository;
    protected final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public StoreResult storeProductImage(String barcode, String filename, byte[] data) {
        var product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No product found with barcode: " + barcode));

        // Determine sort order based on existing images
        var images = product.getImages();
        int nextOrder = (images != null ? images.size() : 0) + 1;

        String path = store(barcode, filename, data);

        ProductImage image = ProductImage.builder()
                .product(product)
                .url("/api/files/" + path)
                .sortOrder(nextOrder)
                .isPrimary(nextOrder == 1)
                .build();
        productImageRepository.save(image);

        return new StoreResult(path, "/api/files/" + path, barcode);
    }
}
