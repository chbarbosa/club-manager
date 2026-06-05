package com.clubmanager.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TrainerResponse(
        UUID uuid,
        String name,
        String birthCountry,
        String livingCountry,
        LocalDate birthdate,
        Integer age,
        String email,
        String phone,
        LocalDate registerDate,
        LocalDate memberSince,
        boolean active) {
}
