package com.clubmanager.dto;

import java.util.UUID;

public record ClubSetupResponse(UUID uuid, String type, String jsonData) {
}

