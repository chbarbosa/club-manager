package com.clubmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.support-access")
public record SupportAccessConfig(boolean enabled) {
}
