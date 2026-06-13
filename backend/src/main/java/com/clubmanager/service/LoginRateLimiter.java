package com.clubmanager.service;

public interface LoginRateLimiter {

    void ensureAllowed(String username, String clientAddress);

    void recordFailure(String username, String clientAddress);

    void recordSuccess(String username, String clientAddress);
}
