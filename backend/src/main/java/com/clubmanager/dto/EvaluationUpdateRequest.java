package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;

public record EvaluationUpdateRequest(
        String title,
        String ageGroup,
        TeamCategory teamCategory) {
}
