package com.clubmanager.service;

import com.clubmanager.config.AppSecurityConfig;
import com.clubmanager.exception.LoginRateLimitException;
import java.time.Duration;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(prefix = "app.security.login-rate-limit", name = "storage", havingValue = "redis")
public class RedisLoginRateLimitService implements LoginRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLoginRateLimitService.class);
    private static final String KEY_PREFIX = "club-manager:login-rate-limit:";

    private final AppSecurityConfig appSecurityConfig;
    private final AppMetricsService appMetricsService;
    private final StringRedisTemplate redisTemplate;

    public RedisLoginRateLimitService(
            AppSecurityConfig appSecurityConfig,
            AppMetricsService appMetricsService,
            StringRedisTemplate redisTemplate) {
        this.appSecurityConfig = appSecurityConfig;
        this.appMetricsService = appMetricsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void ensureAllowed(String username, String clientAddress) {
        if (!appSecurityConfig.loginRateLimit().enabled()) {
            return;
        }
        String value = redisTemplate.opsForValue().get(key(username, clientAddress));
        if (value != null && Long.parseLong(value) >= appSecurityConfig.loginRateLimit().maxFailures()) {
            appMetricsService.recordLoginBlocked();
            LOGGER.warn("Login blocked by Redis rate limit username={} clientAddress={}", cleanUsername(username), cleanClientAddress(clientAddress));
            throw new LoginRateLimitException();
        }
    }

    @Override
    public void recordFailure(String username, String clientAddress) {
        if (!appSecurityConfig.loginRateLimit().enabled()) {
            return;
        }
        String key = key(username, clientAddress);
        Long failures = redisTemplate.opsForValue().increment(key);
        if (failures != null && failures == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(appSecurityConfig.loginRateLimit().windowMinutes()));
        }
    }

    @Override
    public void recordSuccess(String username, String clientAddress) {
        redisTemplate.delete(key(username, clientAddress));
    }

    private String key(String username, String clientAddress) {
        return KEY_PREFIX + cleanUsername(username).toLowerCase(Locale.ROOT) + ":" + cleanClientAddress(clientAddress);
    }

    private String cleanUsername(String username) {
        return StringUtils.hasText(username) ? username.trim() : "unknown";
    }

    private String cleanClientAddress(String clientAddress) {
        return StringUtils.hasText(clientAddress) ? clientAddress.trim() : "unknown";
    }
}
