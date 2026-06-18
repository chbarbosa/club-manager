package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import java.time.LocalDate;

public record EvaluationUpdateRequest(
        String title,
        String ageGroup,
        TeamCategory teamCategory,
        LocalDate limitDate) {
}
