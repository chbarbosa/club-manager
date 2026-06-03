package com.clubmanager.dto;

import java.util.UUID;

public record LoginResponse(String token, UUID adminUuid, String name) {
}

