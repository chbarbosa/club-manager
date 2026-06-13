package com.clubmanager.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.audit.internal-api")
public record AuditInternalApiConfig(
        boolean enabled,
        List<String> allowedCidrs,
        String trustedProxyHeader) {

    public AuditInternalApiConfig {
        allowedCidrs = allowedCidrs == null ? List.of() : List.copyOf(allowedCidrs);
    }
}
