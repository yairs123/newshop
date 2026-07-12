package com.coinmarket.cart.service;

import com.coinmarket.cart.dto.CartItemRequest;
import com.coinmarket.cart.entity.CartItem;
import com.coinmarket.cart.repository.CartItemRepository;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.entity.ProductImage;
import com.coinmarket.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        ProductImage image = new ProductImage();
        image.setUrl("http://example.com/coin.jpg");

        product = Product.builder()
                .title("1895 Morgan Silver Dollar")
                .price(new BigDecimal("1299.99"))
                .currency("USD")
                .stock(5)
                .country("USA")
                .year(1895)
                .material("Silver")
                .ratingCompany("PCGS")
                .ratingGrade("MS63")
                .build();
        product.setId(1L);

        cartItem = CartItem.builder()
                .userId(10L)
                .productId(1L)
                .quantity(2)
                .build();
        cartItem.setId(100L);
    }

    @Nested
    @DisplayName("获取购物车")
    class GetCart {

        @Test
        @DisplayName("返回用户购物车列表")
        void existingCart_returnsItems() {
            given(cartItemRepository.findByUserIdOrderByCreatedAtAsc(10L))
                    .willReturn(List.of(cartItem));
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            var items = cartService.getCart(10L);

            assertThat(items).hasSize(1);
            assertThat(items.get(0).getProductId()).isEqualTo(1L);
            assertThat(items.get(0).getTitle()).isEqualTo("1895 Morgan Silver Dollar");
            assertThat(items.get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("空购物车返回空列表")
        void emptyCart_returnsEmptyList() {
            given(cartItemRepository.findByUserIdOrderByCreatedAtAsc(99L))
                    .willReturn(List.of());

            var items = cartService.getCart(99L);

            assertThat(items).isEmpty();
        }
    }

    @Nested
    @DisplayName("添加商品到购物车")
    class AddItem {

        @Test
        @DisplayName("添加新商品到购物车")
        void newProduct_addsToCart() {
            CartItemRequest request = new CartItemRequest();
            request.setProductId(1L);
            request.setQuantity(1);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(cartItemRepository.findByUserIdAndProductId(10L, 1L))
                    .willReturn(Optional.empty());
            given(cartItemRepository.save(any(CartItem.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            var response = cartService.addItem(10L, request);

            assertThat(response.getProductId()).isEqualTo(1L);
            assertThat(response.getQuantity()).isEqualTo(1);
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("添加已有商品时累加数量")
        void existingProduct_incrementsQuantity() {
            CartItemRequest request = new CartItemRequest();
            request.setProductId(1L);
            request.setQuantity(1);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(cartItemRepository.findByUserIdAndProductId(10L, 1L))
                    .willReturn(Optional.of(cartItem));
            given(cartItemRepository.save(any(CartItem.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            var response = cartService.addItem(10L, request);

            assertThat(response.getQuantity()).isEqualTo(3); // 2 + 1
        }

        @Test
        @DisplayName("添加超过库存时限制为库存量")
        void exceedingStock_capsToStock() {
            CartItemRequest request = new CartItemRequest();
            request.setProductId(1L);
            request.setQuantity(10);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(cartItemRepository.findByUserIdAndProductId(10L, 1L))
                    .willReturn(Optional.empty());
            given(cartItemRepository.save(any(CartItem.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            var response = cartService.addItem(10L, request);

            assertThat(response.getQuantity()).isEqualTo(5); // capped to stock
        }

        @Test
        @DisplayName("商品不存在抛出异常")
        void unknownProduct_throwsBusinessException() {
            CartItemRequest request = new CartItemRequest();
            request.setProductId(999L);
            request.setQuantity(1);

            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(10L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }
    }

    @Nested
    @DisplayName("更新购物车数量")
    class UpdateQuantity {

        @Test
        @DisplayName("更新商品数量成功")
        void existingItem_updatesQuantity() {
            given(cartItemRepository.findByUserIdAndProductId(10L, 1L))
                    .willReturn(Optional.of(cartItem));

            cartService.updateQuantity(10L, 1L, 5);

            verify(cartItemRepository).save(cartItem);
            assertThat(cartItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("商品不在购物车抛出异常")
        void unknownItem_throwsBusinessException() {
            given(cartItemRepository.findByUserIdAndProductId(10L, 999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateQuantity(10L, 999L, 1))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("购物车中不存在该商品");
        }
    }

    @Nested
    @DisplayName("删除购物车商品")
    class RemoveItem {

        @Test
        @DisplayName("删除购物车中的商品")
        void existingItem_removesFromCart() {
            given(cartItemRepository.findByUserIdAndProductId(10L, 1L))
                    .willReturn(Optional.of(cartItem));

            cartService.removeItem(10L, 1L);

            verify(cartItemRepository).delete(cartItem);
        }

        @Test
        @DisplayName("删除不存在的商品不报错")
        void unknownItem_silentlyIgnored() {
            given(cartItemRepository.findByUserIdAndProductId(10L, 999L))
                    .willReturn(Optional.empty());

            cartService.removeItem(10L, 999L);

            verify(cartItemRepository).findByUserIdAndProductId(10L, 999L);
        }
    }

    @Nested
    @DisplayName("清空购物车")
    class ClearCart {

        @Test
        @DisplayName("清空用户购物车")
        void existingCart_clearsAllItems() {
            cartService.clearCart(10L);

            verify(cartItemRepository).deleteByUserId(10L);
        }
    }

    @Nested
    @DisplayName("合并购物车")
    class MergeCart {

        @Test
        @DisplayName("合并本地购物车到用户购物车")
        void localItems_mergedIntoUserCart() {
            CartItemRequest req = new CartItemRequest();
            req.setProductId(1L);
            req.setQuantity(1);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(cartItemRepository.findByUserIdAndProductId(10L, 1L))
                    .willReturn(Optional.of(cartItem));
            given(cartItemRepository.save(any(CartItem.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            cartService.mergeCart(10L, List.of(req));

            verify(cartItemRepository).save(any(CartItem.class));
            assertThat(cartItem.getQuantity()).isEqualTo(3); // 2 + 1, capped to stock 5
        }
    }
}
