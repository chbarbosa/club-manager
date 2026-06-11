package com.clubmanager.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TeamMatchResponse(
        UUID uuid,
        UUID teamUuid,
        String teamIdentification,
        UUID championshipUuid,
        String championshipName,
        String opponent,
        String place,
        LocalDateTime matchDateTime,
        Integer teamScore,
        Integer opponentScore,
        String notes,
        List<MatchPlayerAnalysisResponse> playerAnalyses) {
}
