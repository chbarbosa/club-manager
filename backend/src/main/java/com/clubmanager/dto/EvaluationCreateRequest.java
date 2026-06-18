package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EvaluationCreateRequest(
        @NotBlank String title,
        @NotBlank String ageGroup,
        @NotNull TeamCategory teamCategory,
        LocalDate limitDate) {
}
