package com.clubmanager.dto;

import com.clubmanager.domain.ScheduleType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleCreateRequest(
        @NotNull UUID teamUuid,
        @NotNull UUID fieldUuid,
        @NotNull LocalDateTime dateTime,
        @NotNull Integer durationMinutes,
        @NotNull ScheduleType type,
        String notes) {
}
