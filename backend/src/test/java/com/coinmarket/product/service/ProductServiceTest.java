package com.coinmarket.product.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Category;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.CategoryRepository;
import com.coinmarket.product.repository.ProductRepository;
import com.coinmarket.product.repository.ReviewRepository;
import com.coinmarket.search.service.ProductIndexService;
import com.coinmarket.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RatingLookupService ratingLookupService;

    @Mock
    private ProductIndexService productIndexService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Nothing needed yet
    }

    @Test
    void shouldReturnCategoriesFromRepository() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Coins");
        category.setSlug("coins");
        given(categoryRepository.findAllByOrderBySortOrderAsc()).willReturn(List.of(category));

        List<Category> categories = productService.getCategories();

        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Coins");
    }

    @Test
    void shouldSearchProductsViaRepository() {
        Product product = Product.builder().title("Gold Coin").build();
        product.setId(10L);
        product.setSellerId(0L);
        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);
        given(productRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Product>>any(), any(Pageable.class))).willReturn(page);
        given(userRepository.findById(anyLong())).willReturn(java.util.Optional.empty());

        Page<ProductResponse> responsePage = productService.searchProducts(
                "gold", null, null, null, null, null, null, null, PageRequest.of(0, 20));

        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getContent().get(0).getTitle()).isEqualTo("Gold Coin");
    }

    @Test
    void shouldReturnProductAndIncrementViewCount() {
        Product product = Product.builder().title("Gold Coin").viewCount(0).build();
        product.setId(1L);
        product.setSellerId(2L);
        given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        given(userRepository.findById(anyLong())).willReturn(java.util.Optional.empty());

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.getTitle()).isEqualTo("Gold Coin");
        assertThat(product.getViewCount()).isEqualTo(1);
        verify(productRepository).save(product);
    }

    @Test
    void shouldUpdateProductWhenSellerMatches() {
        Product product = Product.builder().title("Silver Coin").sellerId(10L).build();
        product.setId(1L);
        given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        given(userRepository.findById(anyLong())).willReturn(java.util.Optional.empty());

        ProductCreateRequest request = ProductCreateRequest.builder()
                .title("Updated Silver Coin")
                .description("Updated description")
                .price(new BigDecimal("15.99"))
                .currency("USD")
                .stock(5)
                .categoryId(3L)
                .country("US")
                .year(2024)
                .material("Silver")
                .denomination("1oz")
                .weight(new BigDecimal("31.1"))
                .build();

        ProductResponse response = productService.updateProduct(1L, 10L, request);

        assertThat(response.getTitle()).isEqualTo("Updated Silver Coin");
        assertThat(product.getCategoryId()).isEqualTo(3L);
        verify(productIndexService).indexProduct(product);
    }

    @Test
    void shouldThrowWhenSellerDoesNotOwnProduct() {
        Product product = Product.builder().title("Silver Coin").sellerId(20L).build();
        product.setId(1L);
        given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));

        ProductCreateRequest request = ProductCreateRequest.builder()
                .title("Attempted Update")
                .price(new BigDecimal("10.00"))
                .currency("USD")
                .build();

        assertThatThrownBy(() -> productService.updateProduct(1L, 10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权修改此商品");
    }

    @Test
    @DisplayName("应返回关联商品列表")
    void shouldReturnRelatedProductsForCategory() {
        Product product = Product.builder().title("Gold Coin").categoryId(3L).build();
        product.setId(1L);
        product.setSellerId(1L);
        Product related = Product.builder().title("Related Coin").categoryId(3L).build();
        related.setId(2L);
        related.setSellerId(1L);

        given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));
        given(productRepository.findTop6ByCategoryIdAndIdNot(3L, 1L, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                .willReturn(List.of(related));
        given(userRepository.findById(anyLong())).willReturn(java.util.Optional.empty());

        List<ProductResponse> results = productService.getRelatedProducts(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Related Coin");
    }

    @Nested
    @DisplayName("复制商品")
    class CopyProduct {

        @Test
        @DisplayName("复制商品成功")
        void validRequest_returnsCopy() {
            Product original = Product.builder()
                    .sellerId(10L)
                    .title("Gold Coin")
                    .description("Rare gold coin")
                    .price(new BigDecimal("999.99"))
                    .currency("USD")
                    .stock(1)
                    .categoryId(3L)
                    .status("ACTIVE")
                    .country("USA")
                    .year(1900)
                    .material("Gold")
                    .build();
            original.setId(1L);

            given(productRepository.findById(1L)).willReturn(java.util.Optional.of(original));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            ProductResponse response = productService.copyProduct(1L, 10L);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Gold Coin (副本)");
            verify(productRepository).save(any(Product.class));
            verify(productIndexService).indexProduct(any(Product.class));
        }

        @Test
        @DisplayName("非本人商品复制抛出异常")
        void notOwner_throwsBusinessException() {
            Product original = Product.builder().sellerId(20L).title("Gold Coin").build();
            original.setId(1L);

            given(productRepository.findById(1L)).willReturn(java.util.Optional.of(original));

            assertThatThrownBy(() -> productService.copyProduct(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此商品");
        }
    }

    @Nested
    @DisplayName("标记已打印")
    class MarkAsPrinted {

        @Test
        @DisplayName("卖家标记打印成功")
        void sellerMarksAsPrinted() {
            Product product = Product.builder().sellerId(10L).title("Gold Coin").build();
            product.setId(1L);

            given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            ProductResponse response = productService.markAsPrinted(1L, 10L);

            assertThat(response).isNotNull();
            assertThat(product.getPrintedAt()).isNotNull();
        }

        @Test
        @DisplayName("非本人商品标记打印抛出异常")
        void notOwner_throwsBusinessException() {
            Product product = Product.builder().sellerId(20L).title("Gold Coin").build();
            product.setId(1L);

            given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));

            assertThatThrownBy(() -> productService.markAsPrinted(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此商品");
        }

        @Test
        @DisplayName("批量标记打印成功")
        void batchMarkAsPrinted() {
            Product p1 = Product.builder().sellerId(10L).title("Coin 1").build();
            p1.setId(1L);
            Product p2 = Product.builder().sellerId(10L).title("Coin 2").build();
            p2.setId(2L);

            given(productRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(p1, p2));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            productService.batchMarkAsPrinted(List.of(1L, 2L), 10L);

            assertThat(p1.getPrintedAt()).isNotNull();
            assertThat(p2.getPrintedAt()).isNotNull();
            verify(productRepository, org.mockito.Mockito.times(2)).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("批量更新状态")
    class BatchUpdateStatus {

        @Test
        @DisplayName("批量更新成功")
        void validRequest_updatesStatus() {
            Product p1 = Product.builder().sellerId(10L).title("Coin 1").status("ACTIVE").build();
            p1.setId(1L);
            Product p2 = Product.builder().sellerId(10L).title("Coin 2").status("ACTIVE").build();
            p2.setId(2L);

            given(productRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(p1, p2));

            productService.batchUpdateStatus(List.of(1L, 2L), "INACTIVE", 10L);

            assertThat(p1.getStatus()).isEqualTo("INACTIVE");
            assertThat(p2.getStatus()).isEqualTo("INACTIVE");
            verify(productRepository, org.mockito.Mockito.times(2)).save(any(Product.class));
        }

        @Test
        @DisplayName("跳过非本人商品")
        void skipsNonOwnedProducts() {
            Product p1 = Product.builder().sellerId(10L).title("Coin 1").status("ACTIVE").build();
            p1.setId(1L);
            Product p2 = Product.builder().sellerId(99L).title("Other's Coin").status("ACTIVE").build();
            p2.setId(2L);

            given(productRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(p1, p2));

            productService.batchUpdateStatus(List.of(1L, 2L), "INACTIVE", 10L);

            assertThat(p1.getStatus()).isEqualTo("INACTIVE");
            assertThat(p2.getStatus()).isEqualTo("ACTIVE");
            verify(productRepository).save(p1);
        }
    }

    @Nested
    @DisplayName("按条码查询")
    class FindByBarcode {

        @Test
        @DisplayName("找到商品返回响应")
        void existingBarcode_returnsProduct() {
            Product product = Product.builder().sellerId(10L).title("Gold Coin").barcode("BARCODE123").build();
            product.setId(1L);

            given(productRepository.findByBarcode("BARCODE123")).willReturn(java.util.Optional.of(product));
            given(userRepository.findById(anyLong())).willReturn(java.util.Optional.empty());

            ProductResponse response = productService.findByBarcode("BARCODE123");

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Gold Coin");
        }

        @Test
        @DisplayName("不存在的条码抛出异常")
        void unknownBarcode_throwsBusinessException() {
            given(productRepository.findByBarcode("UNKNOWN")).willReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> productService.findByBarcode("UNKNOWN"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }
    }

    @Nested
    @DisplayName("设置商品状态")
    class SetProductStatus {

        @Test
        @DisplayName("设置状态成功")
        void setsStatus() {
            Product product = Product.builder().sellerId(10L).title("Gold Coin").status("ACTIVE").build();
            product.setId(1L);

            given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            productService.setProductStatus(1L, "INACTIVE");

            assertThat(product.getStatus()).isEqualTo("INACTIVE");
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("不存在的商品抛出异常")
        void unknownProduct_throwsBusinessException() {
            given(productRepository.findById(999L)).willReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> productService.setProductStatus(999L, "INACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }
    }
}
