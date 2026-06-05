package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import java.util.UUID;

public record TeamResponse(
        UUID uuid,
        String ageGroup,
        TeamCategory teamCategory,
        UUID trainerUuid,
        String trainerName,
        boolean active) {
}

