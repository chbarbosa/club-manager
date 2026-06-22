package com.clubmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailConfigProperties(
        boolean enabled,
        String from,
        String appUrl) {
}
