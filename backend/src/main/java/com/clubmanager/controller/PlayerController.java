package com.clubmanager.controller;

import com.clubmanager.dto.PlayerCreateRequest;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.PlayerResponse;
import com.clubmanager.dto.PlayerSummaryResponse;
import com.clubmanager.dto.PlayerUpdateRequest;
import com.clubmanager.mapper.PlayerMapper;
import com.clubmanager.service.PlayerService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
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

    public PlayerController(PlayerService playerService, PlayerMapper playerMapper) {
        this.playerService = playerService;
        this.playerMapper = playerMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse createPlayer(@Valid @RequestBody PlayerCreateRequest request) {
        return playerMapper.toResponse(playerService.createPlayer(request));
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

    @PutMapping("/{uuid}")
    public PlayerResponse updatePlayer(@PathVariable UUID uuid, @Valid @RequestBody PlayerUpdateRequest request) {
        return playerMapper.toResponse(playerService.updatePlayer(uuid, request));
    }

    @PatchMapping("/{uuid}/deactivate")
    public PlayerResponse deactivatePlayer(@PathVariable UUID uuid) {
        return playerMapper.toResponse(playerService.deactivatePlayer(uuid));
    }

    @PatchMapping("/{uuid}/reactivate")
    public PlayerResponse reactivatePlayer(@PathVariable UUID uuid) {
        return playerMapper.toResponse(playerService.reactivatePlayer(uuid));
    }
}
