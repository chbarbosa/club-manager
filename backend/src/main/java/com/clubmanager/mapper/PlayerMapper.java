package com.clubmanager.mapper;

import com.clubmanager.domain.Player;
import com.clubmanager.dto.PlayerResponse;
import com.clubmanager.dto.PlayerSummaryResponse;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "age", expression = "java(calculateAge(player.getBirthdate()))")
    PlayerResponse toResponse(Player player);

    @Mapping(target = "age", expression = "java(calculateAge(player.getBirthdate()))")
    PlayerSummaryResponse toSummaryResponse(Player player);

    default int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}
