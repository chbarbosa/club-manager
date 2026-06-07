package com.clubmanager.mapper;

import com.clubmanager.domain.PlayerSkillHistory;
import com.clubmanager.dto.PlayerSkillHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerSkillHistoryMapper {

    @Mapping(target = "changedByAdminUuid", source = "changedByAdmin.uuid")
    @Mapping(target = "changedByAdminName", source = "changedByAdmin.name")
    @Mapping(target = "evaluationEventUuid", source = "evaluationEvent.uuid")
    PlayerSkillHistoryResponse toResponse(PlayerSkillHistory history);
}
