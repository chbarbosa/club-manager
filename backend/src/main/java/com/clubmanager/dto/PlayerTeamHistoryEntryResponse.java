package com.clubmanager.dto;

import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import java.time.LocalDate;
import java.util.UUID;

public record PlayerTeamHistoryEntryResponse(
        UUID assignmentUuid,
        UUID teamUuid,
        String teamIdentification,
        TeamAgeCategory ageCategory,
        TeamCategory teamCategory,
        Integer jerseyNumber,
        LocalDate assignedDate,
        LocalDate removedDate,
        boolean active) {
}
