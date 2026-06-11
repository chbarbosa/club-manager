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
    @Mapping(target = "identification", source = "ageGroup")
    @Mapping(target = "subTrainerUuid", expression = "java(team.getSubTrainer() == null ? null : team.getSubTrainer().getUuid())")
    @Mapping(target = "subTrainerName", expression = "java(team.getSubTrainer() == null ? null : team.getSubTrainer().getName())")
    @Mapping(target = "assistantAdminUuid", expression = "java(team.getAssistantAdmin() == null ? null : team.getAssistantAdmin().getUuid())")
    @Mapping(target = "assistantAdminName", expression = "java(team.getAssistantAdmin() == null ? null : team.getAssistantAdmin().getName())")
    @Mapping(target = "advice", ignore = true)
    TeamResponse toResponse(Team team);

    @Mapping(target = "trainerUuid", source = "team.trainer.uuid")
    @Mapping(target = "trainerName", source = "team.trainer.name")
    @Mapping(target = "identification", source = "team.ageGroup")
    @Mapping(target = "subTrainerUuid", expression = "java(team.getSubTrainer() == null ? null : team.getSubTrainer().getUuid())")
    @Mapping(target = "subTrainerName", expression = "java(team.getSubTrainer() == null ? null : team.getSubTrainer().getName())")
    @Mapping(target = "assistantAdminUuid", expression = "java(team.getAssistantAdmin() == null ? null : team.getAssistantAdmin().getUuid())")
    @Mapping(target = "assistantAdminName", expression = "java(team.getAssistantAdmin() == null ? null : team.getAssistantAdmin().getName())")
    TeamResponse toResponse(Team team, com.clubmanager.dto.TeamAdviceResponse advice);

    @Mapping(target = "trainerUuid", source = "trainer.uuid")
    @Mapping(target = "trainerName", source = "trainer.name")
    @Mapping(target = "identification", source = "ageGroup")
    @Mapping(target = "subTrainerUuid", expression = "java(team.getSubTrainer() == null ? null : team.getSubTrainer().getUuid())")
    @Mapping(target = "subTrainerName", expression = "java(team.getSubTrainer() == null ? null : team.getSubTrainer().getName())")
    @Mapping(target = "assistantAdminUuid", expression = "java(team.getAssistantAdmin() == null ? null : team.getAssistantAdmin().getUuid())")
    @Mapping(target = "assistantAdminName", expression = "java(team.getAssistantAdmin() == null ? null : team.getAssistantAdmin().getName())")
    TeamSummaryResponse toSummaryResponse(Team team);
}
