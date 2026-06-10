package com.clubmanager.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChampionshipRosterAssignRequest(
        @NotNull UUID playerUuid,
        @NotNull UUID trainerUuid) {
}
