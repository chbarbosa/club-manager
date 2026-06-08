package com.clubmanager.mapper;

import com.clubmanager.domain.EvaluationResult;
import com.clubmanager.dto.EvaluationResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationResultMapper {

    @Mapping(target = "evaluationUuid", source = "evaluation.uuid")
    @Mapping(target = "playerUuid", source = "player.uuid")
    @Mapping(target = "playerName", source = "player.name")
    @Mapping(target = "sourceEventUuid", expression = "java(evaluationResult.getSourceEvent() == null ? null : evaluationResult.getSourceEvent().getUuid())")
    @Mapping(target = "sourceEventPlace", expression = "java(evaluationResult.getSourceEvent() == null ? null : evaluationResult.getSourceEvent().getPlace())")
    @Mapping(target = "sourceEventDate", expression = "java(evaluationResult.getSourceEvent() == null ? null : evaluationResult.getSourceEvent().getEventDate())")
    EvaluationResultResponse toResponse(EvaluationResult evaluationResult);
}
