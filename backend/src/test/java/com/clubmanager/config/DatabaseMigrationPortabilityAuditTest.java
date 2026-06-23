package com.clubmanager.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DatabaseMigrationPortabilityAuditTest {

    private static final Map<String, Pattern> NON_PORTABLE_PATTERNS = new LinkedHashMap<>();

    static {
        NON_PORTABLE_PATTERNS.put("AUTO_INCREMENT", Pattern.compile("\\bAUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE));
        NON_PORTABLE_PATTERNS.put("RANDOM_UUID()", Pattern.compile("\\bRANDOM_UUID\\s*\\(", Pattern.CASE_INSENSITIVE));
        NON_PORTABLE_PATTERNS.put("IF NOT EXISTS", Pattern.compile("\\bIF\\s+NOT\\s+EXISTS\\b", Pattern.CASE_INSENSITIVE));
        NON_PORTABLE_PATTERNS.put("DROP COLUMN IF EXISTS", Pattern.compile("\\bDROP\\s+COLUMN\\s+IF\\s+EXISTS\\b", Pattern.CASE_INSENSITIVE));
        NON_PORTABLE_PATTERNS.put("ALTER COLUMN", Pattern.compile("\\bALTER\\s+COLUMN\\b", Pattern.CASE_INSENSITIVE));
    }

    @Test
    void migrationAudit_DocumentsDetectedNonPortableSqlPatterns() throws IOException {
        String migrations = readAllMigrations();
        String auditDocument = Files.readString(Path.of("..", "docs", "POSTGRESQL_MIGRATION_AUDIT.md"));

        var detectedPatterns = NON_PORTABLE_PATTERNS.entrySet().stream()
                .filter(entry -> entry.getValue().matcher(migrations).find())
                .map(Map.Entry::getKey)
                .toList();

        assertThat(detectedPatterns)
                .as("Detected non-portable migration patterns should be documented before PostgreSQL is enabled")
                .allSatisfy(pattern -> assertThat(auditDocument).contains(pattern));
    }

    private String readAllMigrations() throws IOException {
        StringBuilder migrations = new StringBuilder();
        try (var paths = Files.list(Path.of("src", "main", "resources", "db", "migration"))) {
            for (Path path : paths.filter(path -> path.getFileName().toString().endsWith(".sql")).toList()) {
                migrations.append(Files.readString(path)).append('\n');
            }
        }
        return migrations.toString();
    }
}
