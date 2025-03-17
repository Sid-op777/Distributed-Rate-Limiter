package com.microproject.rate_limiter.service;

import com.microproject.rate_limiter.RateLimitResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//uses sliding window count to rate limit
@Service
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitEventPublisher eventPublisher;

    private static final int REQUEST_LIMIT = 5; // Max 5 requests per minute
    private static final int TIME_WINDOW_SECONDS = 60; // 1-minute window

    @Autowired
    public RateLimiterService(RedisTemplate<String, String> redisTemplate, RateLimitEventPublisher eventPublisher) {
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
    }

    public RateLimitResponse isAllowed(String clientId) {
        String key = "ratelimit:" + clientId;
        long now = Instant.now().getEpochSecond(); // Current timestamp in seconds

        // Remove outdated timestamps
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, now - TIME_WINDOW_SECONDS);

        // Get the current request count
        Long requestCount = redisTemplate.opsForZSet().size(key);

        // Calculate remaining requests
        long remainingRequests = (requestCount == null) ? REQUEST_LIMIT : Math.max(0, REQUEST_LIMIT - requestCount);

        // Get the next reset time (oldest request + TIME_WINDOW_SECONDS)
        Set<String> timestamps = redisTemplate.opsForZSet().range(key, 0, 0);
        long resetTime = (timestamps == null || timestamps.isEmpty()) ? now + TIME_WINDOW_SECONDS
                : Long.parseLong(timestamps.iterator().next()) + TIME_WINDOW_SECONDS;

        boolean allowed = requestCount != null && requestCount < REQUEST_LIMIT;

        if (allowed) {
            redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
            redisTemplate.expire(key, TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        eventPublisher.sendRateLimitEvent(clientId, allowed); // Send event to RabbitMQ
        return new RateLimitResponse(allowed, remainingRequests - (allowed ? 1 : 0), resetTime);
    }
}