package com.clubmanager.dto;

import java.util.List;

public record PlayerTeamHistoryResponse(
        long championshipCount,
        List<PlayerTeamHistoryEntryResponse> teams) {
}
