package com.clubmanager.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TrainerSummaryResponse(
        UUID uuid,
        String name,
        String email,
        LocalDate memberSince,
        boolean active) {
}
