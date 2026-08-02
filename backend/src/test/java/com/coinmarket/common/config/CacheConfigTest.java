package com.coinmarket.common.config;

import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the Redis cache value serializer round-trips the types cached by
 * {@link CacheConfig} (category list and product detail DTO) without losing
 * concrete types. Regression guard for the issue where a cached
 * {@code List<Category>} deserialized its elements as {@code LinkedHashMap}
 * because polymorphic type info was not enabled on the custom ObjectMapper.
 */
class CacheConfigTest {

    private final GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(CacheConfig.redisObjectMapper());

    @Test
    void roundTripsCategoryListAsTypedCategoryInstances() {
        Category coins = Category.builder().id(1L).name("Coins").slug("coins").sortOrder(1).build();
        Category banknotes = Category.builder().id(2L).name("Banknotes").slug("banknotes").sortOrder(2).build();

        Object result = serializer.deserialize(serializer.serialize(List.of(coins, banknotes)));

        assertThat(result).isInstanceOf(List.class);
        List<?> list = (List<?>) result;
        assertThat(list).hasSize(2);
        assertThat(list.get(0)).isInstanceOf(Category.class);
        assertThat(list.get(1)).isInstanceOf(Category.class);
        Category first = (Category) list.get(0);
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getName()).isEqualTo("Coins");
        assertThat(((Category) list.get(1)).getSlug()).isEqualTo("banknotes");
    }

    @Test
    void roundTripsProductResponsePreservingLocalDateTime() {
        ProductResponse response = ProductResponse.builder()
                .id(10L)
                .title("Gold Coin")
                .price(new BigDecimal("999.99"))
                .createdAt(LocalDateTime.of(2026, 8, 2, 10, 30))
                .printedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .images(List.of("https://example.com/a.jpg"))
                .build();

        ProductResponse result = (ProductResponse) serializer.deserialize(serializer.serialize(response));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Gold Coin");
        assertThat(result.getPrice()).isEqualByComparingTo("999.99");
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 8, 2, 10, 30));
        assertThat(result.getPrintedAt()).isEqualTo(LocalDateTime.of(2026, 8, 1, 9, 0));
        assertThat(result.getImages()).containsExactly("https://example.com/a.jpg");
    }
}
