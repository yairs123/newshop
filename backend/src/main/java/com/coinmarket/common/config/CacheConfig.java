package com.coinmarket.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configures Spring Cache backed by Redis with JSON serialization.
 *
 * <p>Cache TTLs:
 * <ul>
 *   <li>{@code products} — product detail responses, 5 minutes.</li>
 *   <li>{@code categories} — category list, 30 minutes (rarely changes; evicted on writes).</li>
 * </ul>
 */
@Configuration
public class CacheConfig {

    /**
     * ObjectMapper used for Redis cache values. Needs Java 8 time support (the DTOs
     * carry {@code LocalDateTime}) plus polymorphic type info: the
     * {@code GenericJackson2JsonRedisSerializer(ObjectMapper)} constructor does NOT
     * configure default typing (only the no-arg path does), so it is enabled here to
     * keep collections and DTOs (e.g. {@code List<Category>}, {@code ProductResponse})
     * round-tripping correctly.
     */
    @SuppressWarnings("deprecation") // DefaultTyping.EVERYTHING is deprecated but required for List round-tripping
    static ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // EVERYTHING is required (not just NON_FINAL) so the top-level collection
        // type id is written (["java.util.ArrayList", [...]]) — NON_FINAL writes only
        // per-element type ids, which breaks List round-tripping.
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
