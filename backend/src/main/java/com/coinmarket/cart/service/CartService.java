package com.coinmarket.cart.service;

import com.coinmarket.cart.dto.CartItemRequest;
import com.coinmarket.cart.dto.CartItemResponse;
import com.coinmarket.cart.entity.CartItem;
import com.coinmarket.cart.repository.CartItemRepository;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtAsc(userId);
        return items.stream().map(this::toResponse).toList();
    }

    @Transactional
    public CartItemResponse addItem(Long userId, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(CartItem.builder()
                        .userId(userId)
                        .productId(request.getProductId())
                        .quantity(0)
                        .build());

        item.setQuantity(item.getQuantity() + request.getQuantity());
        if (item.getQuantity() > product.getStock()) {
            item.setQuantity(product.getStock());
        }
        cartItemRepository.save(item);
        return toResponse(item, product);
    }

    @Transactional
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new BusinessException("购物车中不存在该商品"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        cartItemRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional
    public void mergeCart(Long userId, List<CartItemRequest> localItems) {
        for (CartItemRequest req : localItems) {
            Product product = productRepository.findById(req.getProductId()).orElse(null);
            if (product == null) continue;

            CartItem existing = cartItemRepository
                    .findByUserIdAndProductId(userId, req.getProductId()).orElse(null);
            if (existing != null) {
                existing.setQuantity(Math.min(existing.getQuantity() + req.getQuantity(), product.getStock()));
                cartItemRepository.save(existing);
            } else {
                cartItemRepository.save(CartItem.builder()
                        .userId(userId)
                        .productId(req.getProductId())
                        .quantity(Math.min(req.getQuantity(), product.getStock()))
                        .build());
            }
        }
    }

    private CartItemResponse toResponse(CartItem item) {
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        if (product == null) return null;
        return toResponse(item, product);
    }

    private CartItemResponse toResponse(CartItem item, Product product) {
        String imageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getUrl() : null;

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .image(imageUrl)
                .country(product.getCountry())
                .year(product.getYear())
                .material(product.getMaterial())
                .ratingCompany(product.getRatingCompany())
                .ratingGrade(product.getRatingGrade())
                .quantity(item.getQuantity())
                .stock(product.getStock())
                .build();
    }
}
