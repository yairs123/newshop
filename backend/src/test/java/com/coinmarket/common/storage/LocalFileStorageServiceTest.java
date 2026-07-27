package com.coinmarket.common.storage;

import com.coinmarket.product.repository.ProductImageRepository;
import com.coinmarket.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Local File Storage Service")
class LocalFileStorageServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @TempDir
    Path tempDir;

    private LocalFileStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService(
                productRepository, productImageRepository, tempDir.toString());
        storageService.init();
    }

    @Nested
    @DisplayName("存储文件")
    class StoreFile {

        @Test
        @DisplayName("存储文件并返回相对路径")
        void storesFile() {
            String barcode = "7620222022022";
            String filename = "obverse.jpg";
            byte[] data = "image-data".getBytes();

            String path = storageService.store(barcode, filename, data);

            assertThat(path).isEqualTo(barcode + "/" + filename);
            assertThat(tempDir.resolve(path)).exists();
        }

        @Test
        @DisplayName("同一条码下存储多个文件")
        void storesMultipleFiles() {
            String barcode = "7620222022022";
            storageService.store(barcode, "front.jpg", "front-data".getBytes());
            storageService.store(barcode, "back.jpg", "back-data".getBytes());

            List<String> files = storageService.listByBarcode(barcode);
            assertThat(files).hasSize(2);
            assertThat(files).containsExactly("back.jpg", "front.jpg");
        }
    }

    @Nested
    @DisplayName("删除文件")
    class DeleteFile {

        @Test
        @DisplayName("删除已存在的文件")
        void deletesExistingFile() throws IOException {
            String barcode = "7620222022022";
            String storedPath = storageService.store(barcode, "test.jpg", "data".getBytes());

            storageService.delete(storedPath);

            assertThat(tempDir.resolve(storedPath)).doesNotExist();
        }

        @Test
        @DisplayName("删除不存在的文件不抛出异常")
        void deletesNonExistentFile_noError() {
            storageService.delete("non-existent/file.jpg");

            assertThat(true).isTrue(); // Should not throw
        }
    }

    @Nested
    @DisplayName("按条码列出文件")
    class ListByBarcode {

        @Test
        @DisplayName("不存在的条码返回空列表")
        void nonExistentBarcode_returnsEmptyList() {
            assertThat(storageService.listByBarcode("NONEXISTENT")).isEmpty();
        }

        @Test
        @DisplayName("空目录返回空列表")
        void emptyDirectory_returnsEmptyList() throws IOException {
            Files.createDirectories(tempDir.resolve("empty-barcode"));

            assertThat(storageService.listByBarcode("empty-barcode")).isEmpty();
        }
    }

    @Nested
    @DisplayName("加载文件")
    class LoadFile {

        @Test
        @DisplayName("加载已存在的文件")
        void loadsExistingFile() {
            String barcode = "7620222022022";
            byte[] original = "hello-world".getBytes();
            storageService.store(barcode, "data.txt", original);

            byte[] loaded = storageService.load(barcode + "/data.txt");

            assertThat(loaded).isEqualTo(original);
        }

        @Test
        @DisplayName("加载不存在的文件返回null")
        void nonExistentFile_returnsNull() {
            byte[] loaded = storageService.load("non-existent/file.txt");

            assertThat(loaded).isNull();
        }
    }

    @Nested
    @DisplayName("基础URL")
    class BaseUrl {

        @Test
        @DisplayName("返回配置的基础URL")
        void returnsBaseUrl() {
            assertThat(storageService.getBaseUrl()).isEqualTo("/api/files");
        }
    }

    @Nested
    @DisplayName("加载为资源")
    class LoadAsResource {

        @Test
        @DisplayName("加载已存在的文件为Resource")
        void loadsExistingFile() {
            String barcode = "7620222022022";
            storageService.store(barcode, "photo.jpg", "photo-data".getBytes());

            var resource = storageService.loadAsResource(barcode + "/photo.jpg");

            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("加载不存在的文件返回null")
        void nonExistentFile_returnsNull() {
            var resource = storageService.loadAsResource("non-existent/file.jpg");

            assertThat(resource).isNull();
        }
    }

    @Nested
    @DisplayName("加载为路径")
    class LoadAsPath {

        @Test
        @DisplayName("返回正确的文件路径")
        void returnsCorrectPath() {
            Path path = storageService.loadAsPath("7620222022022/photo.jpg");

            assertThat(path).isEqualTo(tempDir.resolve("7620222022022/photo.jpg").normalize());
        }
    }
}
