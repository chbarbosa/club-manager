package com.clubmanager.mapper;

import com.clubmanager.domain.ClubSetup;
import com.clubmanager.dto.ClubSetupResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClubSetupMapper {

    ClubSetupResponse toResponse(ClubSetup setup);
}

