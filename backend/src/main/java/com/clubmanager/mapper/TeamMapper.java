package com.clubmanager.mapper;

import com.clubmanager.domain.Team;
import com.clubmanager.dto.TeamResponse;
import com.clubmanager.dto.TeamSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target = "trainerUuid", source = "trainer.uuid")
    @Mapping(target = "trainerName", source = "trainer.name")
    TeamResponse toResponse(Team team);

    @Mapping(target = "trainerUuid", source = "trainer.uuid")
    @Mapping(target = "trainerName", source = "trainer.name")
    TeamSummaryResponse toSummaryResponse(Team team);
}

