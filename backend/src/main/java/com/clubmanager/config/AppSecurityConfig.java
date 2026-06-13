package com.clubmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityConfig(
        PasswordPolicy password,
        LoginRateLimit loginRateLimit) {

    public record PasswordPolicy(int minLength) {
    }

    public record LoginRateLimit(boolean enabled, int maxFailures, int windowMinutes, String storage) {
    }
}
