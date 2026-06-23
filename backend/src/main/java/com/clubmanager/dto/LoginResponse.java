package com.clubmanager.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String token,
        UUID adminUuid,
        UUID userUuid,
        String name,
        String role,
        List<String> availableRoles,
        boolean multipleRoles) {
}
