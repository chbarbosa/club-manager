package com.clubmanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PlayerTeamAssignRequest(
        @NotNull UUID playerUuid,
        @NotNull @Min(1) @Max(99) Integer jerseyNumber) {
}
