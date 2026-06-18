package com.clubmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChampionshipCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID teamUuid,
        @NotNull Integer startMonth,
        @NotNull Integer startYear,
        @NotNull Integer endMonth,
        @NotNull Integer endYear,
        @NotNull @Min(0) Integer expectedMatches) {
}
