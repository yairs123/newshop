package com.coinmarket.common.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Simple Redis-backed rate limiter for authentication endpoints.
 * Uses a sliding window approach: counts requests within a fixed time window.
 */
@Component
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if a request is allowed for the given key within the rate limit.
     *
     * @param key       unique identifier (e.g., "login:127.0.0.1")
     * @param maxAttempts maximum number of allowed attempts
     * @param windowSeconds time window in seconds
     * @return true if the request is allowed, false if rate limited
     */
    public boolean isAllowed(String key, int maxAttempts, int windowSeconds) {
        String redisKey = "ratelimit:" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count == null) {
            return true; // Redis unavailable, allow through
        }
        if (count == 1) {
            redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }
        return count <= maxAttempts;
    }

    /**
     * Get remaining attempts for a key.
     */
    public long getRemainingAttempts(String key, int maxAttempts) {
        String redisKey = "ratelimit:" + key;
        String count = redisTemplate.opsForValue().get(redisKey);
        if (count == null) {
            return maxAttempts;
        }
        long used = Long.parseLong(count);
        return Math.max(0, maxAttempts - used);
    }

    /**
     * Reset the rate limit counter for a key (e.g., after successful login).
     */
    public void reset(String key) {
        redisTemplate.delete("ratelimit:" + key);
    }
}
