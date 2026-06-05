package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import java.time.LocalDate;
import java.util.UUID;

public record PlayerTeamResponse(
        UUID uuid,
        UUID playerUuid,
        String playerName,
        TeamCategory playerTeamCategory,
        UUID teamUuid,
        String teamAgeGroup,
        LocalDate assignedDate,
        LocalDate removedDate,
        boolean active) {
}

