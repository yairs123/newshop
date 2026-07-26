package com.coinmarket.common.storage;

import java.util.List;

public interface FileStorageService {

    /**
     * Store a file under the given barcode directory.
     * @return the relative path of the stored file
     */
    String store(String barcode, String filename, byte[] data);

    /**
     * Delete a file by its relative path.
     */
    void delete(String path);

    /**
     * Get the base URL for serving files.
     */
    String getBaseUrl();

    /**
     * List all stored files for a given barcode directory.
     */
    List<String> listByBarcode(String barcode);

    /**
     * Load file bytes for a given relative path.
     * @return file bytes, or null if not found
     */
    byte[] load(String path);

    /**
     * Store a product image: saves the file to storage and creates the corresponding
     * ProductImage database record linked to the product identified by barcode.
     *
     * @param barcode product barcode
     * @param filename sanitized filename
     * @param data file bytes
     * @return result containing path, url, and barcode
     * @throws IllegalArgumentException if no product found with the given barcode
     */
    StoreResult storeProductImage(String barcode, String filename, byte[] data);

    /**
     * Result of a product image store operation.
     */
    record StoreResult(String path, String url, String barcode) {}
}
