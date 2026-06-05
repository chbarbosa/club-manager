package com.clubmanager.mapper;

import com.clubmanager.domain.EvaluationEventAttendance;
import com.clubmanager.dto.EvaluationEventAttendanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationEventAttendanceMapper {

    @Mapping(target = "eventUuid", source = "evaluationEvent.uuid")
    @Mapping(target = "playerUuid", source = "player.uuid")
    @Mapping(target = "playerName", source = "player.name")
    EvaluationEventAttendanceResponse toResponse(EvaluationEventAttendance attendance);
}
