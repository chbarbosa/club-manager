package com.clubmanager.mapper;

import com.clubmanager.domain.Admin;
import com.clubmanager.dto.AdminResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    AdminResponse toResponse(Admin admin);
}

