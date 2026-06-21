package com.clubmanager.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TrainerAccessInviteRequest(@NotNull UUID trainerUuid) {
}
