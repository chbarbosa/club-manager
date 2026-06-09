package com.clubmanager.mapper;

import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.dto.PlayerTeamResponse;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerTeamMapper {

    @Mapping(target = "playerUuid", source = "player.uuid")
    @Mapping(target = "playerName", source = "player.name")
    @Mapping(target = "playerAge", expression = "java(calculateAge(playerTeam.getPlayer().getBirthdate()))")
    @Mapping(target = "playerTeamCategory", source = "player.teamCategory")
    @Mapping(target = "playerPositions", source = "player.positions")
    @Mapping(target = "teamUuid", source = "team.uuid")
    @Mapping(target = "teamIdentification", source = "team.ageGroup")
    @Mapping(target = "teamAgeGroup", source = "team.ageGroup")
    PlayerTeamResponse toResponse(PlayerTeam playerTeam);

    default int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}
