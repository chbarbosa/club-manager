package com.clubmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.UUID;

public record TeamMatchCreateRequest(
        UUID championshipUuid,
        @NotBlank String opponent,
        @NotBlank String place,
        @NotNull LocalDateTime matchDateTime,
        @PositiveOrZero Integer teamScore,
        @PositiveOrZero Integer opponentScore,
        String notes) {
}
