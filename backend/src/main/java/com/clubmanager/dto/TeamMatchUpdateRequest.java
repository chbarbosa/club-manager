package com.clubmanager.dto;

import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.UUID;

public record TeamMatchUpdateRequest(
        UUID championshipUuid,
        String opponent,
        String place,
        LocalDateTime matchDateTime,
        @PositiveOrZero Integer teamScore,
        @PositiveOrZero Integer opponentScore,
        String notes) {
}
