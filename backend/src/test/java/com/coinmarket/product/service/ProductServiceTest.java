package com.coinmarket.product.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Category;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.CategoryRepository;
import com.coinmarket.product.repository.ProductRepository;
import com.coinmarket.search.service.ProductIndexService;
import com.coinmarket.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
}
