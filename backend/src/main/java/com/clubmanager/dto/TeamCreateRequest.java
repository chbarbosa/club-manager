package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TeamCreateRequest(
        @NotBlank String ageGroup,
        @NotNull TeamCategory teamCategory,
        @NotNull UUID trainerUuid) {
}

