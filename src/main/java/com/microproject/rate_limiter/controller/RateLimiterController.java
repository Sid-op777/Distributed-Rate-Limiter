package com.microproject.rate_limiter.controller;


import com.microproject.rate_limiter.RateLimitResponse;
import com.microproject.rate_limiter.service.RateLimiterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratelimit")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    //Dependency Injection by spring boot(IoC)
    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping
    public ResponseEntity<String> checkRateLimit(@RequestParam String clientId) {
        RateLimitResponse response = rateLimiterService.isAllowed(clientId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Remaining", String.valueOf(response.getRemainingRequests()));
        headers.add("X-RateLimit-Reset", String.valueOf(response.getResetTime()));

        if (response.isAllowed()) {
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body("Request successful");
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(headers)
                    .body("Rate limit exceeded. Try again later.");
        }
    }
}
