package com.clubmanager.mapper;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.dto.EvaluationResponse;
import com.clubmanager.dto.EvaluationSummaryResponse;
import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationMapper {

    @Mapping(target = "expired", expression = "java(isExpired(evaluation))")
    EvaluationResponse toResponse(Evaluation evaluation);

    @Mapping(target = "expired", expression = "java(isExpired(evaluation))")
    EvaluationSummaryResponse toSummaryResponse(Evaluation evaluation);

    default boolean isExpired(Evaluation evaluation) {
        return evaluation.getLimitDate() != null
                && evaluation.getStatus() != EvaluationStatus.FINALIZED
                && evaluation.getLimitDate().isBefore(LocalDate.now());
    }
}
