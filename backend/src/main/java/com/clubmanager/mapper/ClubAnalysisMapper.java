package com.clubmanager.mapper;

import com.clubmanager.domain.ClubAnalysis;
import com.clubmanager.domain.ClubAnalysisItem;
import com.clubmanager.domain.ClubAnalysisSeverity;
import com.clubmanager.dto.ClubAnalysisAffectedRecordResponse;
import com.clubmanager.dto.ClubAnalysisItemResponse;
import com.clubmanager.dto.ClubAnalysisResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClubAnalysisMapper {

    private static final TypeReference<List<ClubAnalysisAffectedRecordResponse>> AFFECTED_RECORDS = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ClubAnalysisMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ClubAnalysisResponse toResponse(ClubAnalysis analysis) {
        List<ClubAnalysisItemResponse> items = analysis.getItems().stream()
                .sorted(Comparator
                        .comparing((ClubAnalysisItem item) -> severityOrder(item.getSeverity()))
                        .thenComparing(ClubAnalysisItem::getCode))
                .map(this::toItemResponse)
                .toList();
        return new ClubAnalysisResponse(
                analysis.getUuid(),
                analysis.getAnalysisDate(),
                analysis.getGeneratedAt(),
                items.size(),
                count(items, ClubAnalysisSeverity.INFO),
                count(items, ClubAnalysisSeverity.WARNING),
                count(items, ClubAnalysisSeverity.CRITICAL),
                items);
    }

    private ClubAnalysisItemResponse toItemResponse(ClubAnalysisItem item) {
        return new ClubAnalysisItemResponse(
                item.getUuid(),
                item.getCode(),
                item.getSeverity(),
                item.getTitle(),
                item.getMessage(),
                parseAffectedRecords(item.getAffectedRecords()));
    }

    private List<ClubAnalysisAffectedRecordResponse> parseAffectedRecords(String jsonData) {
        try {
            return objectMapper.readValue(jsonData, AFFECTED_RECORDS);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored club analysis affected records must be valid JSON arrays", exception);
        }
    }

    private int count(List<ClubAnalysisItemResponse> items, ClubAnalysisSeverity severity) {
        return (int) items.stream()
                .filter(item -> item.severity() == severity)
                .count();
    }

    private int severityOrder(ClubAnalysisSeverity severity) {
        if (severity == ClubAnalysisSeverity.CRITICAL) {
            return 0;
        }
        if (severity == ClubAnalysisSeverity.WARNING) {
            return 1;
        }
        return 2;
    }
}
