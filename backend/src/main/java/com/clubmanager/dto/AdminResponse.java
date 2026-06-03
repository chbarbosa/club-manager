package com.clubmanager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminResponse(UUID uuid, String name, String email, String username, LocalDateTime createdAt, boolean active) {
}
