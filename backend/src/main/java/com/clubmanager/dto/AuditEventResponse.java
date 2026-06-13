package com.clubmanager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditEventResponse(
        UUID uuid,
        LocalDateTime occurredAt,
        UUID actorAdminUuid,
        String actorName,
        String action,
        String entityType,
        UUID entityUuid,
        String entityLabel,
        String message) {
}
