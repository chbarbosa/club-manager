package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record PlayerUpdateRequest(
        String name,
        String birthCountry,
        String livingCountry,
        @Past LocalDate birthdate,
        TeamCategory teamCategory,
        String registrationNumber,
        LocalDate memberSince) {
}
