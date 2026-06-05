package com.clubmanager.controller;

import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.TeamCreateRequest;
import com.clubmanager.dto.TeamResponse;
import com.clubmanager.dto.TeamSummaryResponse;
import com.clubmanager.dto.TeamUpdateRequest;
import com.clubmanager.mapper.TeamMapper;
import com.clubmanager.service.TeamService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@PreAuthorize("hasRole('ADMIN')")
public class TeamController {

    private final TeamService teamService;
    private final TeamMapper teamMapper;

    public TeamController(TeamService teamService, TeamMapper teamMapper) {
        this.teamService = teamService;
        this.teamMapper = teamMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@Valid @RequestBody TeamCreateRequest request) {
        return teamMapper.toResponse(teamService.createTeam(request));
    }

    @GetMapping
    public PageResponse<TeamSummaryResponse> getAllTeams(
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) TeamCategory teamCategory,
            Pageable pageable) {
        return PageResponse.from(teamService.searchTeams(ageGroup, teamCategory, pageable)
                .map(teamMapper::toSummaryResponse));
    }

    @GetMapping("/{uuid}")
    public TeamResponse getTeamByUuid(@PathVariable UUID uuid) {
        return teamMapper.toResponse(teamService.getTeamByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public TeamResponse updateTeam(@PathVariable UUID uuid, @Valid @RequestBody TeamUpdateRequest request) {
        return teamMapper.toResponse(teamService.updateTeam(uuid, request));
    }

    @PatchMapping("/{uuid}/deactivate")
    public TeamResponse deactivateTeam(@PathVariable UUID uuid) {
        return teamMapper.toResponse(teamService.deactivateTeam(uuid));
    }

    @PatchMapping("/{uuid}/reactivate")
    public TeamResponse reactivateTeam(@PathVariable UUID uuid) {
        return teamMapper.toResponse(teamService.reactivateTeam(uuid));
    }
}

