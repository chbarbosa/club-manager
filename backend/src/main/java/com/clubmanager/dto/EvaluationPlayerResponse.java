package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import java.time.LocalDate;
import java.util.UUID;

public record EvaluationPlayerResponse(
        UUID uuid,
        UUID playerUuid,
        String playerName,
        TeamCategory playerTeamCategory,
        LocalDate assignedDate,
        boolean active) {
}
