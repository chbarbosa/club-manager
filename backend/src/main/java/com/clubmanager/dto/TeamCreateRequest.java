package com.clubmanager.dto;

import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TeamCreateRequest(
        @NotBlank String identification,
        @NotNull TeamAgeCategory ageCategory,
        @NotNull TeamCategory teamCategory,
        @NotNull UUID trainerUuid,
        UUID subTrainerUuid,
        UUID assistantAdminUuid) {
}
