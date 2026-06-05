package com.clubmanager.mapper;

import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.TrainerResponse;
import com.clubmanager.dto.TrainerSummaryResponse;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "age", expression = "java(calculateAge(trainer.getBirthdate()))")
    TrainerResponse toResponse(Trainer trainer);

    TrainerSummaryResponse toSummaryResponse(Trainer trainer);

    default Integer calculateAge(LocalDate birthdate) {
        return birthdate == null ? null : Period.between(birthdate, LocalDate.now()).getYears();
    }
}
