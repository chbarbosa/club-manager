package com.clubmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TrainerPasswordConfirmRequest(
        @Email @NotBlank String email,
        @NotBlank String code,
        @NotBlank String password) {
}
