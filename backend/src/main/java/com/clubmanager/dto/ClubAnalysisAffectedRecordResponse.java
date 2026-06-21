package com.clubmanager.dto;

import java.util.UUID;

public record ClubAnalysisAffectedRecordResponse(
        String entityType,
        UUID uuid,
        String label) {
}
