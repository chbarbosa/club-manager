package com.clubmanager.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DatabaseMigrationGuardTest {

    private final DatabaseMigrationGuard guard = new DatabaseMigrationGuard();

    @Test
    void validate_WithH2Datasource_AllowsMigration() {
        assertThatCode(() -> guard.validate(
                "jdbc:h2:mem:club-manager-test;DB_CLOSE_DELAY=-1",
                new DatabaseConfigProperties(false)))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_WithNonH2Datasource_FailsClosedByDefault() {
        assertThatThrownBy(() -> guard.validate(
                "jdbc:postgresql://localhost:5432/club_manager",
                new DatabaseConfigProperties(false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Non-H2 datasource detected")
                .hasMessageContaining("H2-specific SQL");
    }

    @Test
    void validate_WithNonH2DatasourceAndExplicitOverride_AllowsMigration() {
        assertThatCode(() -> guard.validate(
                "jdbc:postgresql://localhost:5432/club_manager",
                new DatabaseConfigProperties(true)))
                .doesNotThrowAnyException();
    }
}
