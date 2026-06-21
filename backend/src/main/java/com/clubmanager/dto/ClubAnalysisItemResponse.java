package com.clubmanager.dto;

import com.clubmanager.domain.ClubAnalysisSeverity;
import java.util.List;
import java.util.UUID;

public record ClubAnalysisItemResponse(
        UUID uuid,
        String code,
        ClubAnalysisSeverity severity,
        String title,
        String message,
        List<ClubAnalysisAffectedRecordResponse> affectedRecords) {
}
