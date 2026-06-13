package com.clubmanager.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.clubmanager.service.AppMetricsService;
import com.clubmanager.service.LoginRateLimitService;
import com.clubmanager.service.RedisLoginRateLimitService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

class LoginRateLimiterConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(AppSecurityConfig.class, () -> new AppSecurityConfig(
                    new AppSecurityConfig.PasswordPolicy(10),
                    new AppSecurityConfig.LoginRateLimit(true, 5, 15, "in-memory")))
            .withBean(AppMetricsService.class, () -> new AppMetricsService(new SimpleMeterRegistry()))
            .withUserConfiguration(RateLimiterTestConfig.class);

    @Test
    void contextUsesInMemoryLimiterByDefault() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(LoginRateLimitService.class);
                    assertThat(context).doesNotHaveBean(RedisLoginRateLimitService.class);
                });
    }

    @Test
    void contextUsesRedisLimiterWhenConfigured() {
        new ApplicationContextRunner()
                .withPropertyValues("app.security.login-rate-limit.storage=redis")
                .withBean(AppSecurityConfig.class, () -> new AppSecurityConfig(
                        new AppSecurityConfig.PasswordPolicy(10),
                        new AppSecurityConfig.LoginRateLimit(true, 5, 15, "redis")))
                .withBean(AppMetricsService.class, () -> new AppMetricsService(new SimpleMeterRegistry()))
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .withUserConfiguration(RateLimiterTestConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisLoginRateLimitService.class);
                    assertThat(context).doesNotHaveBean(LoginRateLimitService.class);
                });
    }

    @Configuration
    @Import({LoginRateLimitService.class, RedisLoginRateLimitService.class})
    static class RateLimiterTestConfig {
    }
}
