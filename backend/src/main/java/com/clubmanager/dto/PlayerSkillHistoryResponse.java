package com.clubmanager.dto;

import com.clubmanager.domain.SkillLevel;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerSkillHistoryResponse(
        UUID uuid,
        SkillLevel skillLevel,
        LocalDateTime changedAt,
        UUID changedByAdminUuid,
        String changedByAdminName,
        UUID evaluationEventUuid,
        String description) {
}
