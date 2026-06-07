package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.SkillLevel;
import java.time.LocalDate;
import java.util.UUID;

public record PlayerSummaryResponse(
        UUID uuid,
        String name,
        LocalDate birthdate,
        int age,
        TeamCategory teamCategory,
        SkillLevel currentSkillLevel,
        LocalDate memberSince,
        boolean active) {
}
