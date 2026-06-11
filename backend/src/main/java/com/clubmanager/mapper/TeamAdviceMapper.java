package com.clubmanager.mapper;

import com.clubmanager.domain.TeamAdvice;
import com.clubmanager.domain.TeamAdviceItem;
import com.clubmanager.dto.TeamAdviceItemResponse;
import com.clubmanager.dto.TeamAdviceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamAdviceMapper {

    TeamAdviceResponse toResponse(TeamAdvice advice);

    TeamAdviceItemResponse toResponse(TeamAdviceItem item);
}
