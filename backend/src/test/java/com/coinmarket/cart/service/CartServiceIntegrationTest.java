package com.coinmarket.cart.service;

import com.coinmarket.cart.dto.CartItemRequest;
import com.coinmarket.cart.dto.CartItemResponse;
import com.coinmarket.cart.entity.CartItem;
import com.coinmarket.cart.repository.CartItemRepository;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Service-level integration test verifying {@link CartService} works end-to-end
 * against the real JPA layer backed by H2 (test profile).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CartService Integration Test")
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void seedProduct() {
        product = productRepository.save(Product.builder()
                .sellerId(2L)
                .title("1895 Morgan Silver Dollar")
                .description("A collectible silver coin")
                .price(new BigDecimal("1299.99"))
                .currency("USD")
                .stock(5)
                .status("ACTIVE")
                .viewCount(0)
                .salesCount(0)
                .build());
    }

    private CartItemRequest addRequest(int quantity) {
        return CartItemRequest.builder()
                .productId(product.getId())
                .quantity(quantity)
                .build();
    }

    @Test
    @DisplayName("添加商品到购物车并持久化")
    void addItem_persistsCartItem() {
        CartItemResponse response = cartService.addItem(10L, addRequest(2));

        assertThat(response.getProductId()).isEqualTo(product.getId());
        assertThat(response.getTitle()).isEqualTo("1895 Morgan Silver Dollar");
        assertThat(response.getQuantity()).isEqualTo(2);

        // Verify the cart item hit the database
        assertThat(cartItemRepository.findByUserIdAndProductId(10L, product.getId())).isPresent();
        assertThat(cartItemRepository.findByUserIdAndProductId(10L, product.getId())
                .orElseThrow().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("重复添加同一商品时累加数量并限制不超过库存")
    void addItem_accumulatesQuantity_cappedAtStock() {
        cartService.addItem(10L, addRequest(2));
        cartService.addItem(10L, addRequest(2));

        CartItem stored = cartItemRepository.findByUserIdAndProductId(10L, product.getId()).orElseThrow();
        assertThat(stored.getQuantity()).isEqualTo(4);

        // Adding beyond stock caps at stock level
        cartService.addItem(10L, addRequest(10));
        stored = cartItemRepository.findByUserIdAndProductId(10L, product.getId()).orElseThrow();
        assertThat(stored.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("获取购物车返回已持久化商品")
    void getCart_returnsPersistedItems() {
        cartService.addItem(10L, addRequest(1));
        cartService.addItem(10L, addRequest(1));

        List<CartItemResponse> items = cartService.getCart(10L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getTitle()).isEqualTo("1895 Morgan Silver Dollar");
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
        assertThat(items.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("1299.99"));
    }

    @Test
    @DisplayName("更新购物车数量并持久化")
    void updateQuantity_updatesPersistedCartItem() {
        cartItemRepository.save(CartItem.builder()
                .userId(10L)
                .productId(product.getId())
                .quantity(2)
                .build());

        cartService.updateQuantity(10L, product.getId(), 5);

        CartItem stored = cartItemRepository.findByUserIdAndProductId(10L, product.getId()).orElseThrow();
        assertThat(stored.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("更新不存在的购物车项抛出异常")
    void updateQuantity_unknownItem_throws() {
        assertThatThrownBy(() -> cartService.updateQuantity(10L, 999999L, 5))
                .isInstanceOf(BusinessException.class)
                .hasMessage("购物车中不存在该商品");
    }

    @Test
    @DisplayName("商品不存在时添加购物车抛出异常")
    void addItem_unknownProduct_throws() {
        CartItemRequest request = CartItemRequest.builder()
                .productId(999999L)
                .quantity(1)
                .build();

        assertThatThrownBy(() -> cartService.addItem(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("商品不存在");
    }
}
