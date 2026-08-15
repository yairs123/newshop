package com.coinmarket.cart.repository;

import com.coinmarket.cart.entity.CartItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("CartItem Repository Integration Test")
class CartItemRepositoryIT {

    @Autowired
    private CartItemRepository cartItemRepository;

    private CartItem buildCartItem(Long userId, Long productId, int quantity) {
        return CartItem.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .build();
    }

    @Test
    @DisplayName("保存购物车项并可通过用户+商品查询")
    void saveAndFindByUserAndProduct() {
        CartItem item = buildCartItem(10L, 100L, 2);
        CartItem saved = cartItemRepository.save(item);
        assertThat(saved.getId()).isNotNull();

        Optional<CartItem> found = cartItemRepository.findByUserIdAndProductId(10L, 100L);
        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(2);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("按用户查询购物车项并按创建时间排序")
    void findByUserId_ordersByCreatedAt() {
        cartItemRepository.save(buildCartItem(10L, 100L, 1));
        cartItemRepository.save(buildCartItem(10L, 200L, 3));
        cartItemRepository.save(buildCartItem(10L, 300L, 5));

        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtAsc(10L);
        assertThat(items).hasSize(3);
        assertThat(items).extracting(CartItem::getProductId)
                .containsExactly(100L, 200L, 300L);
    }

    @Test
    @DisplayName("按用户删除购物车项")
    void deleteByUserId_removesAllItems() {
        cartItemRepository.save(buildCartItem(10L, 100L, 1));
        cartItemRepository.save(buildCartItem(10L, 200L, 1));
        cartItemRepository.save(buildCartItem(20L, 300L, 1));

        cartItemRepository.deleteByUserId(10L);

        assertThat(cartItemRepository.findByUserIdOrderByCreatedAtAsc(10L)).isEmpty();
        assertThat(cartItemRepository.findByUserIdOrderByCreatedAtAsc(20L)).hasSize(1);
    }

    @Test
    @DisplayName("统计用户购物车项数量")
    void countByUserId() {
        cartItemRepository.save(buildCartItem(10L, 100L, 1));
        cartItemRepository.save(buildCartItem(10L, 200L, 1));

        assertThat(cartItemRepository.countByUserId(10L)).isEqualTo(2L);
        assertThat(cartItemRepository.countByUserId(99L)).isZero();
    }

    @Test
    @DisplayName("不存在的用户购物车返回空")
    void unknownUser_returnsEmpty() {
        Optional<CartItem> found = cartItemRepository.findByUserIdAndProductId(999L, 100L);
        assertThat(found).isEmpty();

        assertThat(cartItemRepository.findByUserIdOrderByCreatedAtAsc(999L)).isEmpty();
    }
}
