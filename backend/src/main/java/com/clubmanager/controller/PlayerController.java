package com.clubmanager.controller;

import com.clubmanager.dto.PlayerCreateRequest;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.PlayerResponse;
import com.clubmanager.dto.PlayerSkillHistoryResponse;
import com.clubmanager.dto.PlayerSummaryResponse;
import com.clubmanager.dto.PlayerUpdateRequest;
import com.clubmanager.mapper.PlayerMapper;
import com.clubmanager.mapper.PlayerSkillHistoryMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.PlayerService;
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
@RequestMapping("/api/v1/players")
@PreAuthorize("hasRole('ADMIN')")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerMapper playerMapper;
    private final PlayerSkillHistoryMapper playerSkillHistoryMapper;
    private final AuditEventService auditEventService;

    public PlayerController(
            PlayerService playerService,
            PlayerMapper playerMapper,
            PlayerSkillHistoryMapper playerSkillHistoryMapper,
            AuditEventService auditEventService) {
        this.playerService = playerService;
        this.playerMapper = playerMapper;
        this.playerSkillHistoryMapper = playerSkillHistoryMapper;
        this.auditEventService = auditEventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    public PageResponse<PlayerSummaryResponse> getAllPlayers(@RequestParam(required = false) String name, Pageable pageable) {
        return PageResponse.from(playerService.searchPlayers(name, pageable)
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

    @PutMapping("/{uuid}")
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
