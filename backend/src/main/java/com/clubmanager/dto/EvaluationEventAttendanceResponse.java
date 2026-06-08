package com.clubmanager.dto;

import com.clubmanager.domain.EvaluationAttendanceStatus;
import java.util.UUID;

public record EvaluationEventAttendanceResponse(
        UUID uuid,
        UUID eventUuid,
        UUID playerUuid,
        String playerName,
        EvaluationAttendanceStatus status,
        String reason) {
}
