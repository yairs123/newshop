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
}
