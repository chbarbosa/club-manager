package com.clubmanager.dto;

import java.util.UUID;

public record ClubFieldResponse(
        UUID uuid,
        String name,
        String location,
        boolean active) {
}
