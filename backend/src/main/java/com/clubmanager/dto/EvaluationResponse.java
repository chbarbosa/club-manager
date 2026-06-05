package com.clubmanager.dto;

import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.TeamCategory;
import java.time.LocalDate;
import java.util.UUID;

public record EvaluationResponse(
        UUID uuid,
        String title,
        EvaluationStatus status,
        String ageGroup,
        TeamCategory teamCategory,
        LocalDate createdDate) {
}
