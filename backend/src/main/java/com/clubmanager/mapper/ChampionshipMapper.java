package com.clubmanager.mapper;

import com.clubmanager.domain.Championship;
import com.clubmanager.dto.ChampionshipResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChampionshipMapper {

    @Mapping(target = "teamUuid", source = "team.uuid")
    @Mapping(target = "teamIdentification", source = "team.ageGroup")
    @Mapping(target = "teamAgeCategory", source = "team.ageCategory")
    @Mapping(target = "teamCategory", source = "team.teamCategory")
    @Mapping(target = "trainerName", source = "team.trainer.name")
    ChampionshipResponse toResponse(Championship championship);
}
