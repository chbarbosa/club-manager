package com.clubmanager.mapper;

import com.clubmanager.domain.ChampionshipRoster;
import com.clubmanager.dto.ChampionshipRosterResponse;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChampionshipRosterMapper {

    @Mapping(target = "championshipUuid", source = "championship.uuid")
    @Mapping(target = "playerUuid", source = "player.uuid")
    @Mapping(target = "playerName", source = "player.name")
    @Mapping(target = "playerAge", expression = "java(calculateAge(roster.getPlayer().getBirthdate()))")
    @Mapping(target = "playerTeamCategory", source = "player.teamCategory")
    @Mapping(target = "playerPositions", source = "player.positions")
    @Mapping(target = "trainerUuid", source = "trainer.uuid")
    @Mapping(target = "trainerName", source = "trainer.name")
    ChampionshipRosterResponse toResponse(ChampionshipRoster roster);

    default int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}
