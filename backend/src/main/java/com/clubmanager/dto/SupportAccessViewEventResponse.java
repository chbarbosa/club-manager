package com.clubmanager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupportAccessViewEventResponse(
        UUID uuid,
        LocalDateTime occurredAt,
        String feature,
        String httpMethod,
        String path,
        UUID entityUuid) {
}
