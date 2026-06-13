package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.config.AppSecurityConfig;
import com.clubmanager.exception.LoginRateLimitException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisLoginRateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisLoginRateLimitService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new RedisLoginRateLimitService(
                securityConfig(),
                new AppMetricsService(new SimpleMeterRegistry()),
                redisTemplate);
    }

    @Test
    void ensureAllowed_WhenRedisCountAtLimit_BlocksLogin() {
        when(valueOperations.get("club-manager:login-rate-limit:admin:127.0.0.1")).thenReturn("5");

        assertThatThrownBy(() -> service.ensureAllowed("admin", "127.0.0.1"))
                .isInstanceOf(LoginRateLimitException.class);
    }

    @Test
    void recordFailure_WhenFirstFailure_SetsTtl() {
        when(valueOperations.increment("club-manager:login-rate-limit:admin:127.0.0.1")).thenReturn(1L);

        service.recordFailure("admin", "127.0.0.1");

        verify(redisTemplate).expire("club-manager:login-rate-limit:admin:127.0.0.1", Duration.ofMinutes(15));
    }

    @Test
    void recordSuccess_RemovesRateLimitKey() {
        service.recordSuccess("admin", "127.0.0.1");

        verify(redisTemplate).delete("club-manager:login-rate-limit:admin:127.0.0.1");
    }

    private AppSecurityConfig securityConfig() {
        return new AppSecurityConfig(
                new AppSecurityConfig.PasswordPolicy(10),
                new AppSecurityConfig.LoginRateLimit(true, 5, 15, "redis"));
    }
}
