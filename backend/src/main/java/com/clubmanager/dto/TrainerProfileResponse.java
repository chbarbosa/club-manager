package com.clubmanager.dto;

import java.util.List;

public record TrainerProfileResponse(
        TrainerResponse trainer,
        List<TrainerTeamHistoryResponse> teams) {
}
