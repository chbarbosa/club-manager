package com.clubmanager.dto;

import java.util.List;

public record MatchPlayerAnalysisUpdateRequest(
        List<String> improvementTags,
        List<String> highlightTags,
        String notes) {
}
