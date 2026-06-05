package com.clubmanager.mapper;

import com.clubmanager.domain.EvaluationPlayer;
import com.clubmanager.dto.EvaluationPlayerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationPlayerMapper {

    @Mapping(target = "playerUuid", source = "player.uuid")
    @Mapping(target = "playerName", source = "player.name")
    @Mapping(target = "playerTeamCategory", source = "player.teamCategory")
    EvaluationPlayerResponse toResponse(EvaluationPlayer evaluationPlayer);
}
