package com.coinmarket.common.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    private Path rootPath;

    @PostConstruct
    public void init() {
        rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + rootPath, e);
        }
        log.info("File storage initialized at: {}", rootPath);
    }

    @Override
    public String store(String barcode, String filename, byte[] data) {
        try {
            Path barcodeDir = rootPath.resolve(barcode);
            Files.createDirectories(barcodeDir);
            Path target = barcodeDir.resolve(filename);
            Files.write(target, data);
            String relativePath = barcode + "/" + filename;
            log.info("Stored file: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + barcode + "/" + filename, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path target = rootPath.resolve(path);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", path, e);
        }
    }

    @Override
    public String getBaseUrl() {
        return "/api/files";
    }

    @Override
    public List<String> listByBarcode(String barcode) {
        Path barcodeDir = rootPath.resolve(barcode);
        if (!Files.exists(barcodeDir)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(barcodeDir)) {
            return stream
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Failed to list files for barcode: {}", barcode, e);
            return Collections.emptyList();
        }
    }

    public Path loadAsPath(String relativePath) {
        return rootPath.resolve(relativePath).normalize();
    }

    public Resource loadAsResource(String relativePath) {
        try {
            Path file = loadAsPath(relativePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            return null;
        } catch (MalformedURLException e) {
            log.warn("Invalid file path: {}", relativePath, e);
            return null;
        }
    }
}
