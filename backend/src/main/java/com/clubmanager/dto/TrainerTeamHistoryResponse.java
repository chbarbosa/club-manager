package com.clubmanager.dto;

import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import java.util.UUID;

public record TrainerTeamHistoryResponse(
        UUID teamUuid,
        String teamIdentification,
        TeamAgeCategory ageCategory,
        TeamCategory teamCategory,
        String role,
        boolean active) {
}
