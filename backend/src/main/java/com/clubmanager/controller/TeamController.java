package com.clubmanager.controller;

import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.TeamCreateRequest;
import com.clubmanager.dto.TeamResponse;
import com.clubmanager.dto.TeamSummaryResponse;
import com.clubmanager.dto.TeamUpdateRequest;
import com.clubmanager.mapper.TeamMapper;
import com.clubmanager.mapper.TeamAdviceMapper;
import com.clubmanager.domain.Team;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.TeamAdviceService;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/teams")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamMapper teamMapper;
    private final TeamAdviceService teamAdviceService;
    private final TeamAdviceMapper teamAdviceMapper;
    private final AuditEventService auditEventService;



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse createTeam(@Valid @RequestBody TeamCreateRequest request) {
        Team team = teamService.createTeam(request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.TEAM,
                team.getUuid(),
                teamLabel(team),
                "Team created: " + teamLabel(team));
        return teamMapper.toResponse(team);
    }

    @GetMapping
    public PageResponse<TeamSummaryResponse> getAllTeams(
            @RequestParam(required = false) String identification,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) TeamCategory teamCategory,
            Pageable pageable) {
        String effectiveIdentification = identification != null ? identification : ageGroup;
        return PageResponse.from(teamService.searchTeams(effectiveIdentification, teamCategory, pageable)
                .map(teamMapper::toSummaryResponse));
    }

    @GetMapping("/{uuid}")
    public TeamResponse getTeamByUuid(@PathVariable UUID uuid) {
        Team team = teamService.getTeamByUuid(uuid);
        return teamMapper.toResponse(team, teamAdviceMapper.toResponse(teamAdviceService.analyze(team)));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse updateTeam(@PathVariable UUID uuid, @Valid @RequestBody TeamUpdateRequest request) {
        Team team = teamService.updateTeam(uuid, request);
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.TEAM,
                team.getUuid(),
                teamLabel(team),
                "Team updated: " + teamLabel(team));
        return teamMapper.toResponse(team);
    }

    @PatchMapping("/{uuid}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse deactivateTeam(@PathVariable UUID uuid) {
        Team team = teamService.deactivateTeam(uuid);
        auditEventService.record(
                AuditEventService.DEACTIVATED,
                AuditEventService.TEAM,
                team.getUuid(),
                teamLabel(team),
                "Team deactivated: " + teamLabel(team));
        return teamMapper.toResponse(team);
    }

    @PatchMapping("/{uuid}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse reactivateTeam(@PathVariable UUID uuid) {
        Team team = teamService.reactivateTeam(uuid);
        auditEventService.record(
                AuditEventService.REACTIVATED,
                AuditEventService.TEAM,
                team.getUuid(),
                teamLabel(team),
                "Team reactivated: " + teamLabel(team));
        return teamMapper.toResponse(team);
    }

    private String teamLabel(Team team) {
        return team.getAgeGroup() + " " + team.getTeamCategory();
    }
}
