package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.SkillLevel;
import java.time.LocalDate;
import java.util.UUID;

public record PlayerResponse(
        UUID uuid,
        String name,
        String birthCountry,
        String livingCountry,
        LocalDate birthdate,
        int age,
        TeamCategory teamCategory,
        SkillLevel currentSkillLevel,
        String registrationNumber,
        LocalDate registerDate,
        LocalDate memberSince,
        boolean active) {
}
