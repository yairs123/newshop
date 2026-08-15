package com.coinmarket.product.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.order.repository.OrderRepository;
import com.coinmarket.product.dto.ReviewRequest;
import com.coinmarket.product.dto.ReviewResponse;
import com.coinmarket.product.dto.ReviewSummary;
import com.coinmarket.product.entity.Review;
import com.coinmarket.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        // Verify user has purchased this product (order completed)
        boolean hasPurchased = orderRepository.existsByBuyerIdAndItemsProductIdAndStatus(
                userId, request.getProductId(), "COMPLETED");
        if (!hasPurchased) {
            throw new BusinessException("只有购买过的用户才能评价");
        }
        // Check not already reviewed
        if (reviewRepository.existsByProductIdAndUserId(request.getProductId(), userId)) {
            throw new BusinessException("您已经评价过该商品");
        }
        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(userId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReviewSummary getProductSummary(Long productId) {
        Double avg = reviewRepository.averageRatingByProductId(productId);
        Long count = reviewRepository.countByProductId(productId);
        return ReviewSummary.builder()
                .averageRating(avg != null ? avg : 0.0)
                .totalReviews(count != null ? count : 0)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean hasReviewed(Long userId, Long productId) {
        return reviewRepository.existsByProductIdAndUserId(productId, userId);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
