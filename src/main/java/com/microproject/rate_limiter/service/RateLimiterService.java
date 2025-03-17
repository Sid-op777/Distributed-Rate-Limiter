package com.microproject.rate_limiter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private static final int REQUEST_LIMIT = 5; // Max 5 requests per minute
    private static final Duration TIME_WINDOW = Duration.ofMinutes(1);

    public RateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientId) {
        String key = "ratelimit:" + clientId;
        ValueOperations<String, String> operations = redisTemplate.opsForValue();

        // Get current request count
        String requestCount = operations.get(key);
        int currentCount = (requestCount != null) ? Integer.parseInt(requestCount) : 0;

//        logger.info("Client: {} - Current Requests: {}", clientId, currentCount);

        if (currentCount < REQUEST_LIMIT) {
            operations.increment(key);
            redisTemplate.expire(key, TIME_WINDOW);
            logger.info("Client: {} - Request allowed ✅ - New Count: {}", clientId, currentCount + 1);
            return true;
        } else {
            logger.warn("Client: {} - Rate limit exceeded ❌", clientId);
            return false;
        }
    }
}