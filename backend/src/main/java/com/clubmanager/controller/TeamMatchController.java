package com.clubmanager.controller;

import com.clubmanager.domain.TeamMatch;
import com.clubmanager.dto.MatchPlayerAnalysisResponse;
import com.clubmanager.dto.MatchPlayerAnalysisUpdateRequest;
import com.clubmanager.dto.TeamMatchCreateRequest;
import com.clubmanager.dto.TeamMatchResponse;
import com.clubmanager.dto.TeamMatchUpdateRequest;
import com.clubmanager.mapper.TeamMatchMapper;
import com.clubmanager.service.TeamMatchService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams/{teamUuid}/matches")
@PreAuthorize("hasRole('ADMIN')")
public class TeamMatchController {

    private final TeamMatchService teamMatchService;
    private final TeamMatchMapper teamMatchMapper;

    public TeamMatchController(TeamMatchService teamMatchService, TeamMatchMapper teamMatchMapper) {
        this.teamMatchService = teamMatchService;
        this.teamMatchMapper = teamMatchMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMatchResponse createMatch(
            @PathVariable UUID teamUuid,
            @Valid @RequestBody TeamMatchCreateRequest request) {
        return teamMatchMapper.toSummaryResponse(teamMatchService.createMatch(teamUuid, request));
    }

    @GetMapping
    public List<TeamMatchResponse> getTeamMatches(@PathVariable UUID teamUuid) {
        return teamMatchService.getTeamMatches(teamUuid).stream()
                .map(teamMatchMapper::toSummaryResponse)
                .toList();
    }

    @GetMapping("/{matchUuid}")
    public TeamMatchResponse getTeamMatch(@PathVariable UUID teamUuid, @PathVariable UUID matchUuid) {
        TeamMatch match = teamMatchService.getTeamMatch(teamUuid, matchUuid);
        return teamMatchMapper.toDetailResponse(
                match,
                teamMatchService.getCurrentRoster(teamUuid),
                teamMatchService.getAnalysesByPlayer(match));
    }

    @PutMapping("/{matchUuid}")
    public TeamMatchResponse updateMatch(
            @PathVariable UUID teamUuid,
            @PathVariable UUID matchUuid,
            @Valid @RequestBody TeamMatchUpdateRequest request) {
        return teamMatchMapper.toSummaryResponse(teamMatchService.updateMatch(teamUuid, matchUuid, request));
    }

    @PutMapping("/{matchUuid}/players/{playerUuid}")
    public MatchPlayerAnalysisResponse savePlayerAnalysis(
            @PathVariable UUID teamUuid,
            @PathVariable UUID matchUuid,
            @PathVariable UUID playerUuid,
            @RequestBody MatchPlayerAnalysisUpdateRequest request) {
        return teamMatchMapper.toPlayerAnalysisResponse(
                teamMatchService.savePlayerAnalysis(teamUuid, matchUuid, playerUuid, request));
    }
}
