package com.clubmanager.service;

import com.clubmanager.config.AppSecurityConfig;
import com.clubmanager.exception.LoginRateLimitException;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(prefix = "app.security.login-rate-limit", name = "storage", havingValue = "in-memory", matchIfMissing = true)
public class LoginRateLimitService implements LoginRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginRateLimitService.class);

    private final AppSecurityConfig appSecurityConfig;
    private final AppMetricsService appMetricsService;
    private final Clock clock;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginRateLimitService(AppSecurityConfig appSecurityConfig, AppMetricsService appMetricsService) {
        this(appSecurityConfig, appMetricsService, Clock.systemUTC());
    }

    public LoginRateLimitService(AppSecurityConfig appSecurityConfig, AppMetricsService appMetricsService, Clock clock) {
        this.appSecurityConfig = appSecurityConfig;
        this.appMetricsService = appMetricsService;
        this.clock = clock;
    }

    @Override
    public void ensureAllowed(String username, String clientAddress) {
        if (!appSecurityConfig.loginRateLimit().enabled()) {
            return;
        }
        AttemptWindow window = attempts.get(key(username, clientAddress));
        if (window != null && !window.isExpired(now(), windowMinutes())
                && window.failures() >= appSecurityConfig.loginRateLimit().maxFailures()) {
            appMetricsService.recordLoginBlocked();
            LOGGER.warn("Login blocked by rate limit username={} clientAddress={}", cleanUsername(username), cleanClientAddress(clientAddress));
            throw new LoginRateLimitException();
        }
    }

    @Override
    public void recordFailure(String username, String clientAddress) {
        if (!appSecurityConfig.loginRateLimit().enabled()) {
            return;
        }
        attempts.compute(key(username, clientAddress), (ignored, existing) -> {
            Instant current = now();
            if (existing == null || existing.isExpired(current, windowMinutes())) {
                return new AttemptWindow(1, current);
            }
            return new AttemptWindow(existing.failures() + 1, existing.startedAt());
        });
    }

    @Override
    public void recordSuccess(String username, String clientAddress) {
        attempts.remove(key(username, clientAddress));
    }

    private String key(String username, String clientAddress) {
        return cleanUsername(username).toLowerCase(Locale.ROOT) + "|" + cleanClientAddress(clientAddress);
    }

    private String cleanUsername(String username) {
        return StringUtils.hasText(username) ? username.trim() : "unknown";
    }

    private String cleanClientAddress(String clientAddress) {
        return StringUtils.hasText(clientAddress) ? clientAddress.trim() : "unknown";
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private long windowMinutes() {
        return appSecurityConfig.loginRateLimit().windowMinutes();
    }

    private record AttemptWindow(int failures, Instant startedAt) {

        boolean isExpired(Instant now, long windowMinutes) {
            return startedAt.plusSeconds(windowMinutes * 60).isBefore(now);
        }
    }
}
