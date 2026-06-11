package com.clubmanager.dto;

import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.TeamCategory;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record MatchPlayerAnalysisResponse(
        UUID uuid,
        UUID playerUuid,
        String playerName,
        int playerAge,
        TeamCategory playerTeamCategory,
        Set<PlayerPosition> playerPositions,
        List<String> improvementTags,
        List<String> highlightTags,
        String notes) {
}
