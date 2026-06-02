package com.clubmanager.mapper;

import com.clubmanager.domain.Club;
import com.clubmanager.dto.ClubResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClubMapper {

    ClubResponse toResponse(Club club);
}

