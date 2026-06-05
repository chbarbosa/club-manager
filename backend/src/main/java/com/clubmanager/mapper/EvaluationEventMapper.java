package com.clubmanager.mapper;

import com.clubmanager.domain.EvaluationEvent;
import com.clubmanager.dto.EvaluationEventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationEventMapper {

    @Mapping(target = "evaluationUuid", source = "evaluation.uuid")
    EvaluationEventResponse toResponse(EvaluationEvent evaluationEvent);
}
