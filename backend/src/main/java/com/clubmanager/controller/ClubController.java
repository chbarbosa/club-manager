package com.clubmanager.controller;

import com.clubmanager.dto.ClubResponse;
import com.clubmanager.dto.ClubSetupResponse;
import com.clubmanager.dto.ClubSetupUpdateRequest;
import com.clubmanager.dto.ClubUpdateRequest;
import com.clubmanager.mapper.ClubMapper;
import com.clubmanager.mapper.ClubSetupMapper;
import com.clubmanager.service.ClubService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/club")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;
    private final ClubMapper clubMapper;
    private final ClubSetupMapper clubSetupMapper;



    @GetMapping
    public ClubResponse getClub() {
        return clubMapper.toResponse(clubService.getClub());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ClubResponse updateClub(@Valid @RequestBody ClubUpdateRequest request) {
        return clubMapper.toResponse(clubService.updateClub(request));
    }

    @GetMapping("/setup")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClubSetupResponse> getAllSetup() {
        return clubService.getAllSetup().stream()
                .map(clubSetupMapper::toResponse)
                .toList();
    }

    @GetMapping("/setup/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClubSetupResponse getSetupByType(@PathVariable String type) {
        return clubSetupMapper.toResponse(clubService.getSetupByType(type));
    }

    @PutMapping("/setup/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClubSetupResponse updateSetup(
            @PathVariable UUID uuid,
            @Valid @RequestBody ClubSetupUpdateRequest request
    ) {
        return clubSetupMapper.toResponse(clubService.updateSetup(uuid, request));
    }
}
