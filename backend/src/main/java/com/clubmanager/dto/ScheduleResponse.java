package com.clubmanager.dto;

import com.clubmanager.domain.ScheduleStatus;
import com.clubmanager.domain.ScheduleType;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleResponse(
        UUID uuid,
        UUID teamUuid,
        String teamIdentification,
        String teamCategory,
        UUID fieldUuid,
        String fieldName,
        String fieldLocation,
        LocalDateTime dateTime,
        int durationMinutes,
        ScheduleType type,
        ScheduleStatus status,
        String notes,
        String cancelReason) {
}
