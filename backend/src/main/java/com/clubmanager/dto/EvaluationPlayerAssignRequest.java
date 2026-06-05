package com.clubmanager.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EvaluationPlayerAssignRequest(@NotNull UUID playerUuid) {
}
