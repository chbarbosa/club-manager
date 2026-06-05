package com.clubmanager.dto;

import com.clubmanager.domain.EvaluationEventStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record EvaluationEventResponse(
        UUID uuid,
        UUID evaluationUuid,
        String place,
        LocalDate eventDate,
        LocalTime startTime,
        int durationMinutes,
        EvaluationEventStatus status,
        String cancelReason) {
}
