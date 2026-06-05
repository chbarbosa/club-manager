package com.clubmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record EvaluationEventCreateRequest(
        @NotBlank String place,
        @NotNull LocalDate eventDate,
        @NotNull LocalTime startTime,
        @NotNull Integer durationMinutes) {
}
