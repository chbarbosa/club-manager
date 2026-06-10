package com.clubmanager.dto;

import java.util.UUID;

public record ChampionshipUpdateRequest(
        String name,
        String description,
        UUID teamUuid,
        Integer startMonth,
        Integer startYear,
        Integer endMonth,
        Integer endYear) {
}
