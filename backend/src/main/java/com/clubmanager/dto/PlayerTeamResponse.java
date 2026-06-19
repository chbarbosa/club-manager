package com.clubmanager.dto;

import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.TeamCategory;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record PlayerTeamResponse(
        UUID uuid,
        UUID playerUuid,
        String playerName,
        int playerAge,
        TeamCategory playerTeamCategory,
        Set<PlayerPosition> playerPositions,
        UUID teamUuid,
        String teamIdentification,
        String teamAgeGroup,
        Integer jerseyNumber,
        LocalDate assignedDate,
        LocalDate removedDate,
        boolean active) {
}
