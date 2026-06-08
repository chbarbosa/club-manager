package com.clubmanager.dto;

import com.clubmanager.domain.EvaluationAttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record EvaluationEventAttendanceUpdateRequest(
        @NotNull EvaluationAttendanceStatus status,
        String reason) {
}
