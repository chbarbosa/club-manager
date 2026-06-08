package com.clubmanager.dto;

import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import java.util.UUID;

public record TeamUpdateRequest(
        String identification,
        TeamAgeCategory ageCategory,
        TeamCategory teamCategory,
        UUID trainerUuid,
        UUID subTrainerUuid,
        UUID assistantAdminUuid) {
}
