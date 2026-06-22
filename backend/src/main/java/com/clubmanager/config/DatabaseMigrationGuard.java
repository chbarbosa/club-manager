package com.clubmanager.config;

import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationGuard {

    void validate(String datasourceUrl, DatabaseConfigProperties properties) {
        if (isH2(datasourceUrl) || properties.allowNonH2()) {
            return;
        }
        throw new IllegalStateException("""
                Non-H2 datasource detected, but PostgreSQL/vendor migration support is not enabled yet. \
                Current Flyway migrations contain H2-specific SQL. Keep using H2, or complete the \
                vendor-specific migration slice before setting app.database.allow-non-h2=true.""");
    }

    private boolean isH2(String datasourceUrl) {
        return datasourceUrl != null && datasourceUrl.toLowerCase().startsWith("jdbc:h2:");
    }
}
