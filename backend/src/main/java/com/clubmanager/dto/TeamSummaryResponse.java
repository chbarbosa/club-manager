package com.clubmanager.dto;

import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import java.util.UUID;

public record TeamSummaryResponse(
        UUID uuid,
        String identification,
        String ageGroup,
        TeamAgeCategory ageCategory,
        TeamCategory teamCategory,
        UUID trainerUuid,
        String trainerName,
        UUID subTrainerUuid,
        String subTrainerName,
        UUID assistantAdminUuid,
        String assistantAdminName,
        boolean active) {
}
