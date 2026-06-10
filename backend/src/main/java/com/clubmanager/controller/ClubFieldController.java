package com.clubmanager.controller;

import com.clubmanager.dto.ClubFieldResponse;
import com.clubmanager.mapper.ClubFieldMapper;
import com.clubmanager.service.ClubFieldService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fields")
@PreAuthorize("hasRole('ADMIN')")
public class ClubFieldController {

    private final ClubFieldService clubFieldService;
    private final ClubFieldMapper clubFieldMapper;

    public ClubFieldController(ClubFieldService clubFieldService, ClubFieldMapper clubFieldMapper) {
        this.clubFieldService = clubFieldService;
        this.clubFieldMapper = clubFieldMapper;
    }

    @GetMapping
    public List<ClubFieldResponse> getActiveFields() {
        return clubFieldService.getActiveFields().stream()
                .map(clubFieldMapper::toResponse)
                .toList();
    }
}
