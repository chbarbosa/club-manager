package com.clubmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerPasswordResetConfirmRequest(
        @NotBlank String code,
        @NotBlank String password) {
}
