package com.clubmanager.dto;

import com.clubmanager.domain.EvaluationAttendanceStatus;
import com.clubmanager.domain.SkillLevel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EvaluationResultResponse(
        UUID uuid,
        UUID evaluationUuid,
        UUID playerUuid,
        String playerName,
        UUID sourceEventUuid,
        String sourceEventPlace,
        LocalDate sourceEventDate,
        SkillLevel levelResult,
        EvaluationAttendanceStatus attendanceStatus,
        LocalDateTime finalizedAt) {
}
