package com.clubmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record TrainerUpdateRequest(
        String name,
        String birthCountry,
        String livingCountry,
        @Past LocalDate birthdate,
        @Email String email,
        String phone,
        LocalDate memberSince) {
}
