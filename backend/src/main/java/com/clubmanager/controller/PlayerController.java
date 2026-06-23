package com.clubmanager.controller;

import com.clubmanager.dto.PlayerCreateRequest;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.PlayerResponse;
import com.clubmanager.dto.PlayerSkillHistoryResponse;
import com.clubmanager.dto.PlayerSummaryResponse;
import com.clubmanager.dto.PlayerTeamHistoryEntryResponse;
import com.clubmanager.dto.PlayerTeamHistoryResponse;
import com.clubmanager.dto.PlayerUpdateRequest;
import com.clubmanager.mapper.PlayerMapper;
import com.clubmanager.mapper.PlayerSkillHistoryMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.PlayerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/players")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'TRAINER')")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerMapper playerMapper;
    private final PlayerSkillHistoryMapper playerSkillHistoryMapper;
    private final AuditEventService auditEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerResponse createPlayer(@Valid @RequestBody PlayerCreateRequest request) {
        var player = playerService.createPlayer(request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.PLAYER,
                player.getUuid(),
                player.getName(),
                "Player created: " + player.getName());
        return playerMapper.toResponse(player);
    }

    @GetMapping
    public PageResponse<PlayerSummaryResponse> getAllPlayers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return PageResponse.from(playerService.searchPlayers(name, active, pageable)
                .map(playerMapper::toSummaryResponse));
    }

    @GetMapping("/{uuid}")
    public PlayerResponse getPlayerByUuid(@PathVariable UUID uuid) {
        return playerMapper.toResponse(playerService.getPlayerByUuid(uuid));
    }

    @GetMapping("/{uuid}/skill-history")
    public java.util.List<PlayerSkillHistoryResponse> getPlayerSkillHistory(@PathVariable UUID uuid) {
        return playerService.getSkillHistory(uuid).stream()
                .map(playerSkillHistoryMapper::toResponse)
                .toList();
    }

    @GetMapping("/{uuid}/teams")
    public PlayerTeamHistoryResponse getPlayerTeamHistory(@PathVariable UUID uuid) {
        return new PlayerTeamHistoryResponse(
                playerService.countChampionships(uuid),
                playerService.getTeamHistory(uuid).stream()
                        .map(assignment -> new PlayerTeamHistoryEntryResponse(
                                assignment.getUuid(),
                                assignment.getTeam().getUuid(),
                                assignment.getTeam().getAgeGroup(),
                                assignment.getTeam().getAgeCategory(),
                                assignment.getTeam().getTeamCategory(),
                                assignment.getJerseyNumber(),
                                assignment.getAssignedDate(),
                                assignment.getRemovedDate(),
                                assignment.isActive()))
                        .toList());
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerResponse updatePlayer(@PathVariable UUID uuid, @Valid @RequestBody PlayerUpdateRequest request) {
        var player = playerService.updatePlayer(uuid, request);
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.PLAYER,
                player.getUuid(),
                player.getName(),
                "Player updated: " + player.getName());
        return playerMapper.toResponse(player);
    }

    @PatchMapping("/{uuid}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerResponse deactivatePlayer(@PathVariable UUID uuid) {
        var player = playerService.deactivatePlayer(uuid);
        auditEventService.record(
                AuditEventService.DEACTIVATED,
                AuditEventService.PLAYER,
                player.getUuid(),
                player.getName(),
                "Player deactivated: " + player.getName());
        return playerMapper.toResponse(player);
    }

    @PatchMapping("/{uuid}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerResponse reactivatePlayer(@PathVariable UUID uuid) {
        var player = playerService.reactivatePlayer(uuid);
        auditEventService.record(
                AuditEventService.REACTIVATED,
                AuditEventService.PLAYER,
                player.getUuid(),
                player.getName(),
                "Player reactivated: " + player.getName());
        return playerMapper.toResponse(player);
    }
}
