package com.microproject.rate_limiter.controller;


import com.microproject.rate_limiter.service.RateLimiterService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratelimit")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping
    public String checkRateLimit(@RequestParam String clientId) {
        boolean allowed = rateLimiterService.isAllowed(clientId);
        return allowed ? "Request allowed ✅" : "Rate limit exceeded ❌";
    }
}
