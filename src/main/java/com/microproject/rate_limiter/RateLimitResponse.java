package com.microproject.rate_limiter;

public class RateLimitResponse {
    private final boolean allowed;
    private final long remainingRequests;
    private final long resetTime;

    public RateLimitResponse(boolean allowed, long remainingRequests, long resetTime) {
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
        this.resetTime = resetTime;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemainingRequests() {
        return remainingRequests;
    }

    public long getResetTime() {
        return resetTime;
    }
}
