package com.clubmanager.dto;

import com.clubmanager.domain.EvaluationAttendanceStatus;
import com.clubmanager.domain.SkillLevel;
import java.util.UUID;

public record EvaluationEventAttendanceResponse(
        UUID uuid,
        UUID eventUuid,
        UUID playerUuid,
        String playerName,
        EvaluationAttendanceStatus status,
        SkillLevel skillLevel,
        String reason) {
}
