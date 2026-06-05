package com.clubmanager.mapper;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.dto.EvaluationResponse;
import com.clubmanager.dto.EvaluationSummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EvaluationMapper {

    EvaluationResponse toResponse(Evaluation evaluation);

    EvaluationSummaryResponse toSummaryResponse(Evaluation evaluation);
}
