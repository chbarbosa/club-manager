package com.clubmanager.dto;

import jakarta.validation.constraints.NotBlank;
import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.NotNull;

public record EvaluationCreateRequest(
        @NotBlank String title,
        @NotBlank String ageGroup,
        @NotNull TeamCategory teamCategory) {
}
