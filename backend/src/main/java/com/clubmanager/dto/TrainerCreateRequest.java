package com.clubmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record TrainerCreateRequest(
        @NotBlank String name,
        String birthCountry,
        String livingCountry,
        @Past LocalDate birthdate,
        @Email String email,
        String phone,
        @NotNull LocalDate memberSince) {
}
