package com.clubmanager.dto;

import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import java.util.UUID;

public record ChampionshipResponse(
        UUID uuid,
        String name,
        String description,
        UUID teamUuid,
        String teamIdentification,
        TeamAgeCategory teamAgeCategory,
        TeamCategory teamCategory,
        String trainerName,
        int startMonth,
        int startYear,
        int endMonth,
        int endYear,
        int expectedMatches,
        boolean active) {
}
