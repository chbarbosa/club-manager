package com.clubmanager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupportAccessResponse(
        UUID uuid,
        String email,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime revokedAt,
        String status,
        UUID createdByAdminUuid,
        String createdByAdminName) {
}
