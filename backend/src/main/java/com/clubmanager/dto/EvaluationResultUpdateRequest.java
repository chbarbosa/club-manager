package com.clubmanager.dto;

import com.clubmanager.domain.SkillLevel;
import jakarta.validation.constraints.NotNull;

public record EvaluationResultUpdateRequest(
        @NotNull SkillLevel levelResult) {
}
