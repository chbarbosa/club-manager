package com.clubmanager.dto;

import java.util.List;

public record TeamAdviceResponse(
        int totalPlayers,
        int minimumPlayersForAdvice,
        int goalkeepers,
        int defenders,
        int midfielders,
        int attackers,
        List<TeamAdviceItemResponse> items) {
}
