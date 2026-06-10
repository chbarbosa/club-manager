package com.clubmanager.controller;

import com.clubmanager.dto.ChampionshipCreateRequest;
import com.clubmanager.dto.ChampionshipResponse;
import com.clubmanager.dto.ChampionshipUpdateRequest;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.mapper.ChampionshipMapper;
import com.clubmanager.service.ChampionshipService;
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
@RequestMapping("/api/v1/championships")
@PreAuthorize("hasRole('ADMIN')")
public class ChampionshipController {

    private final ChampionshipService championshipService;
    private final ChampionshipMapper championshipMapper;

    public ChampionshipController(
            ChampionshipService championshipService,
            ChampionshipMapper championshipMapper) {
        this.championshipService = championshipService;
        this.championshipMapper = championshipMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChampionshipResponse createChampionship(@Valid @RequestBody ChampionshipCreateRequest request) {
        return championshipMapper.toResponse(championshipService.createChampionship(request));
    }

    @GetMapping
    public PageResponse<ChampionshipResponse> getAllChampionships(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID teamUuid,
            Pageable pageable) {
        return PageResponse.from(championshipService.searchChampionships(name, teamUuid, pageable)
                .map(championshipMapper::toResponse));
    }

    @GetMapping("/{uuid}")
    public ChampionshipResponse getChampionshipByUuid(@PathVariable UUID uuid) {
        return championshipMapper.toResponse(championshipService.getChampionshipByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public ChampionshipResponse updateChampionship(
            @PathVariable UUID uuid,
            @Valid @RequestBody ChampionshipUpdateRequest request) {
        return championshipMapper.toResponse(championshipService.updateChampionship(uuid, request));
    }

    @PatchMapping("/{uuid}/deactivate")
    public ChampionshipResponse deactivateChampionship(@PathVariable UUID uuid) {
        return championshipMapper.toResponse(championshipService.deactivateChampionship(uuid));
    }

    @PatchMapping("/{uuid}/reactivate")
    public ChampionshipResponse reactivateChampionship(@PathVariable UUID uuid) {
        return championshipMapper.toResponse(championshipService.reactivateChampionship(uuid));
    }
}
