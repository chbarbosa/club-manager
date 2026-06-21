package com.clubmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ClubAnalysisResponse(
        UUID uuid,
        LocalDate analysisDate,
        LocalDateTime generatedAt,
        int totalItems,
        int infoCount,
        int warningCount,
        int criticalCount,
        List<ClubAnalysisItemResponse> items) {
}
