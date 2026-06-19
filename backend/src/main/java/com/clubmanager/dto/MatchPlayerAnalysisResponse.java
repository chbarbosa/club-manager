package com.clubmanager.dto;

import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.SkillLevel;
import com.clubmanager.domain.TeamCategory;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record MatchPlayerAnalysisResponse(
        UUID uuid,
        UUID playerUuid,
        String playerName,
        int playerAge,
        SkillLevel playerCurrentSkillLevel,
        TeamCategory playerTeamCategory,
        Set<PlayerPosition> playerPositions,
        long playerChampionshipCount,
        List<String> improvementTags,
        List<String> highlightTags,
        String notes) {
}
