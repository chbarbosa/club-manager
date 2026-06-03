package com.clubmanager.dto;

import com.clubmanager.domain.TeamCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record PlayerCreateRequest(
        @NotBlank String name,
        @NotBlank String birthCountry,
        @NotBlank String livingCountry,
        @NotNull @Past LocalDate birthdate,
        @NotNull TeamCategory teamCategory,
        String registrationNumber,
        @NotNull LocalDate memberSince) {
}
