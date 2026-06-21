package com.clubmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SupportAccessCreateRequest(@Email @NotBlank String email) {
}
