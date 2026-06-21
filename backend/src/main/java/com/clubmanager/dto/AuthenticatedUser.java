package com.clubmanager.dto;

import java.util.UUID;

public record AuthenticatedUser(String username, UUID uuid, String name, String role) {
}
