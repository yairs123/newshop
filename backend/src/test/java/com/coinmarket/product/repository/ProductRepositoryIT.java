package com.coinmarket.product.repository;

import com.coinmarket.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Product Repository Integration Test")
class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("保存商品并通过条码查找")
    void saveAndFindByBarcode() {
        Product product = Product.builder()
                .sellerId(1L)
                .title("Test Silver Dollar")
                .description("A test coin")
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .stock(5)
                .status("ACTIVE")
                .barcode("TESTBARCODE001")
                .viewCount(0)
                .salesCount(0)
                .build();

        Product saved = productRepository.save(product);
        assertThat(saved.getId()).isNotNull();

        Optional<Product> found = productRepository.findByBarcode("TESTBARCODE001");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Silver Dollar");
        assertThat(found.get().getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("不存在的条码返回空")
    void unknownBarcode_returnsEmpty() {
        Optional<Product> found = productRepository.findByBarcode("NONEXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("统计卖家商品数")
    void countBySellerId() {
        Product p1 = Product.builder()
                .sellerId(42L).title("Coin 1").price(new BigDecimal("10.00"))
                .currency("USD").stock(1).status("ACTIVE").viewCount(0).salesCount(0)
                .build();
        Product p2 = Product.builder()
                .sellerId(42L).title("Coin 2").price(new BigDecimal("20.00"))
                .currency("USD").stock(2).status("ACTIVE").viewCount(0).salesCount(0)
                .build();
        productRepository.save(p1);
        productRepository.save(p2);

        long count = productRepository.countBySellerId(42L);
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("条码唯一性约束")
    void barcodeUniqueness() {
        Product p1 = Product.builder()
                .sellerId(1L).title("First Coin").price(new BigDecimal("10.00"))
                .currency("USD").stock(1).status("ACTIVE")
                .barcode("UNIQUE-BARCODE").viewCount(0).salesCount(0)
                .build();
        productRepository.save(p1);

        Product p2 = Product.builder()
                .sellerId(2L).title("Second Coin").price(new BigDecimal("10.00"))
                .currency("USD").stock(1).status("ACTIVE")
                .barcode("DIFFERENT-BARCODE").viewCount(0).salesCount(0)
                .build();
        Product saved = productRepository.save(p2);
        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findByBarcode("DIFFERENT-BARCODE")).isPresent();
    }

    @Test
    @DisplayName("检查条码是否存在")
    void existsByBarcode() {
        Product product = Product.builder()
                .sellerId(1L).title("Barcode Check").price(new BigDecimal("5.00"))
                .currency("USD").stock(1).status("ACTIVE")
                .barcode("EXISTS-CHECK").viewCount(0).salesCount(0)
                .build();
        productRepository.save(product);

        assertThat(productRepository.existsByBarcode("EXISTS-CHECK")).isTrue();
        assertThat(productRepository.existsByBarcode("DOES-NOT-EXIST")).isFalse();
    }
}
