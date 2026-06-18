package com.clubmanager.dto;

import jakarta.validation.constraints.Min;
import java.util.UUID;

public record ChampionshipUpdateRequest(
        String name,
        String description,
        UUID teamUuid,
        Integer startMonth,
        Integer startYear,
        Integer endMonth,
        Integer endYear,
        @Min(0) Integer expectedMatches) {
}
