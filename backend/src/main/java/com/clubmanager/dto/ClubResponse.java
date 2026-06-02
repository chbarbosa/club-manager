package com.clubmanager.dto;

import java.util.UUID;

public record ClubResponse(
        UUID uuid,
        String name,
        String description,
        String colour1,
        String colour2
) {
}

