package com.clubmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record ClubSetupUpdateRequest(@NotBlank String jsonData) {
}

