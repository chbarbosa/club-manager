package com.clubmanager.controller;

import com.clubmanager.dto.PlayerTeamAssignRequest;
import com.clubmanager.dto.PlayerTeamResponse;
import com.clubmanager.mapper.PlayerTeamMapper;
import com.clubmanager.service.PlayerTeamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams/{teamUuid}/players")
@PreAuthorize("hasRole('ADMIN')")
public class PlayerTeamController {

    private final PlayerTeamService playerTeamService;
    private final PlayerTeamMapper playerTeamMapper;

    public PlayerTeamController(PlayerTeamService playerTeamService, PlayerTeamMapper playerTeamMapper) {
        this.playerTeamService = playerTeamService;
        this.playerTeamMapper = playerTeamMapper;
    }

    @GetMapping
    public List<PlayerTeamResponse> getActiveRoster(@PathVariable UUID teamUuid) {
        return playerTeamService.getActiveRoster(teamUuid).stream()
                .map(playerTeamMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerTeamResponse assignPlayer(
            @PathVariable UUID teamUuid,
            @Valid @RequestBody PlayerTeamAssignRequest request) {
        return playerTeamMapper.toResponse(playerTeamService.assignPlayer(teamUuid, request));
    }

    @DeleteMapping("/{assignmentUuid}")
    public PlayerTeamResponse removePlayer(@PathVariable UUID teamUuid, @PathVariable UUID assignmentUuid) {
        return playerTeamMapper.toResponse(playerTeamService.removePlayer(teamUuid, assignmentUuid));
    }
}

