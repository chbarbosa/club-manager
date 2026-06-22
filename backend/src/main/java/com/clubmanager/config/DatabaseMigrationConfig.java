package com.clubmanager.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DatabaseConfigProperties.class)
@RequiredArgsConstructor
public class DatabaseMigrationConfig {

    private final DatabaseMigrationGuard databaseMigrationGuard;
    private final DatabaseConfigProperties databaseConfigProperties;
    private final DataSourceProperties dataSourceProperties;

    @Bean
    FlywayConfigurationCustomizer guardedFlywayConfigurationCustomizer() {
        return this::validateDatasourceBeforeFlyway;
    }

    private void validateDatasourceBeforeFlyway(FluentConfiguration configuration) {
        databaseMigrationGuard.validate(dataSourceProperties.getUrl(), databaseConfigProperties);
    }
}
