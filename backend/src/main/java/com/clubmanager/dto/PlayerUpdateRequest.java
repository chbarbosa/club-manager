package com.clubmanager.dto;

import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Set;

public record PlayerUpdateRequest(
        String name,
        String birthCountry,
        String livingCountry,
        @Past LocalDate birthdate,
        TeamCategory teamCategory,
        Set<PlayerPosition> positions,
        String registrationNumber,
        LocalDate memberSince) {
}
