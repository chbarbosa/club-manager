package com.clubmanager.dto;

import java.util.UUID;

public record LoginResponse(String token, UUID adminUuid, UUID userUuid, String name, String role) {
}
