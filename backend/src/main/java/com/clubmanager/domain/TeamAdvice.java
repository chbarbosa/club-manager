package com.clubmanager.domain;

import java.util.List;

public record TeamAdvice(
        int totalPlayers,
        int minimumPlayersForAdvice,
        int goalkeepers,
        int defenders,
        int midfielders,
        int attackers,
        List<TeamAdviceItem> items) {
}
