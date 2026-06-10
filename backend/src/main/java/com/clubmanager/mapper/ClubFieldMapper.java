package com.clubmanager.mapper;

import com.clubmanager.domain.ClubField;
import com.clubmanager.dto.ClubFieldResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClubFieldMapper {

    ClubFieldResponse toResponse(ClubField field);
}
