package com.clubmanager.dto;

import com.clubmanager.domain.EvaluationAttendanceStatus;
import com.clubmanager.domain.SkillLevel;
import jakarta.validation.constraints.NotNull;

public record EvaluationEventAttendanceUpdateRequest(
        @NotNull EvaluationAttendanceStatus status,
        @NotNull SkillLevel skillLevel,
        String reason) {
}
