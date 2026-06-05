package com.clubmanager.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PlayerTeamAssignRequest(@NotNull UUID playerUuid) {
}

