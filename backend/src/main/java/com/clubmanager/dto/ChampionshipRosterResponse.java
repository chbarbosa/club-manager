package com.clubmanager.dto;

import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.TeamCategory;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ChampionshipRosterResponse(
        UUID uuid,
        UUID championshipUuid,
        UUID playerUuid,
        String playerName,
        int playerAge,
        TeamCategory playerTeamCategory,
        Set<PlayerPosition> playerPositions,
        UUID trainerUuid,
        String trainerName,
        LocalDate assignedDate,
        LocalDate removedDate,
        boolean active) {
}
