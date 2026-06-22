package com.clubmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.database")
public record DatabaseConfigProperties(boolean allowNonH2) {
}
