package com.clubmanager.mapper;

import com.clubmanager.domain.Schedule;
import com.clubmanager.dto.ScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "teamUuid", source = "team.uuid")
    @Mapping(target = "teamIdentification", source = "team.ageGroup")
    @Mapping(target = "teamCategory", source = "team.teamCategory")
    @Mapping(target = "fieldUuid", source = "field.uuid")
    @Mapping(target = "fieldName", source = "field.name")
    @Mapping(target = "fieldLocation", source = "field.location")
    ScheduleResponse toResponse(Schedule schedule);
}
