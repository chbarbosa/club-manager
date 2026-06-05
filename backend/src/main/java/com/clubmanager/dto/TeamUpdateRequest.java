package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import java.util.UUID;

public record TeamUpdateRequest(
        String ageGroup,
        TeamCategory teamCategory,
        UUID trainerUuid) {
}

