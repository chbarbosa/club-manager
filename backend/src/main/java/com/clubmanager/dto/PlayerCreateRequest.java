package com.clubmanager.dto;

import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Set;

public record PlayerCreateRequest(
        @NotBlank String name,
        @NotBlank String birthCountry,
        @NotBlank String livingCountry,
        @NotNull @Past LocalDate birthdate,
        @NotNull TeamCategory teamCategory,
        @NotEmpty Set<PlayerPosition> positions,
        String registrationNumber,
        @NotNull LocalDate memberSince) {
}
