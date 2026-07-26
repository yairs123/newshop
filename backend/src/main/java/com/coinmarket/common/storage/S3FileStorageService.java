package com.coinmarket.common.storage;

import com.coinmarket.product.repository.ProductImageRepository;
import com.coinmarket.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3FileStorageService extends AbstractFileStorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String endpoint;

    private String baseUrl;

    public S3FileStorageService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            S3Client s3Client,
            @Value("${app.storage.s3.bucket:coinmarket}") String bucket,
            @Value("${app.storage.s3.endpoint:}") String endpoint) {
        super(productRepository, productImageRepository);
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.endpoint = endpoint;
    }

    @PostConstruct
    public void init() {
        // Determine base URL for serving files
        if (!endpoint.isEmpty() && !endpoint.contains("localhost")) {
            baseUrl = endpoint + "/" + bucket;
        } else {
            baseUrl = "/api/files";
        }
        log.info("S3FileStorage initialized, bucket: {}, endpoint: {}", bucket, endpoint);
    }

    @Override
    public String store(String barcode, String filename, byte[] data) {
        String key = "uploads/" + barcode + "/" + filename;
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build(), RequestBody.fromBytes(data));
            log.info("S3 upload success: {}", key);
            return key;
        } catch (S3Exception e) {
            throw new RuntimeException("S3 upload failed: " + key, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build());
        } catch (S3Exception e) {
            log.warn("S3 delete failed: {}", path, e);
        }
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public List<String> listByBarcode(String barcode) {
        try {
            var response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix("uploads/" + barcode + "/")
                    .build());
            return response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
        } catch (S3Exception e) {
            log.warn("S3 list failed for: {}", barcode, e);
            return Collections.emptyList();
        }
    }

    @Override
    public byte[] load(String path) {
        String s3Key = path.startsWith("uploads/") ? path : "uploads/" + path;
        try {
            var response = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build());
            return response.readAllBytes();
        } catch (NoSuchKeyException e) {
            log.warn("S3 file not found: {}", s3Key);
            return null;
        } catch (S3Exception | IOException e) {
            log.warn("S3 load failed: {}", s3Key, e);
            return null;
        }
    }
}
