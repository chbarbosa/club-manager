package com.clubmanager.dto;

import com.clubmanager.domain.ScheduleType;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleUpdateRequest(
        UUID teamUuid,
        UUID fieldUuid,
        LocalDateTime dateTime,
        Integer durationMinutes,
        ScheduleType type,
        String notes) {
}
