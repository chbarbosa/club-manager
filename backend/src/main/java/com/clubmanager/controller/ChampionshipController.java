package com.clubmanager.controller;

import com.clubmanager.dto.ChampionshipCreateRequest;
import com.clubmanager.dto.ChampionshipResponse;
import com.clubmanager.dto.ChampionshipUpdateRequest;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.mapper.ChampionshipMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.ChampionshipService;
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
@RequestMapping("/api/v1/championships")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ChampionshipController {

    private final ChampionshipService championshipService;
    private final ChampionshipMapper championshipMapper;
    private final AuditEventService auditEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChampionshipResponse createChampionship(@Valid @RequestBody ChampionshipCreateRequest request) {
        var championship = championshipService.createChampionship(request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.CHAMPIONSHIP,
                championship.getUuid(),
                championship.getName(),
                "Championship created: " + championship.getName());
        return championshipMapper.toResponse(championship);
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
        var championship = championshipService.updateChampionship(uuid, request);
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.CHAMPIONSHIP,
                championship.getUuid(),
                championship.getName(),
                "Championship updated: " + championship.getName());
        return championshipMapper.toResponse(championship);
    }

    @PatchMapping("/{uuid}/deactivate")
    public ChampionshipResponse deactivateChampionship(@PathVariable UUID uuid) {
        var championship = championshipService.deactivateChampionship(uuid);
        auditEventService.record(
                AuditEventService.DEACTIVATED,
                AuditEventService.CHAMPIONSHIP,
                championship.getUuid(),
                championship.getName(),
                "Championship deactivated: " + championship.getName());
        return championshipMapper.toResponse(championship);
    }

    @PatchMapping("/{uuid}/reactivate")
    public ChampionshipResponse reactivateChampionship(@PathVariable UUID uuid) {
        var championship = championshipService.reactivateChampionship(uuid);
        auditEventService.record(
                AuditEventService.REACTIVATED,
                AuditEventService.CHAMPIONSHIP,
                championship.getUuid(),
                championship.getName(),
                "Championship reactivated: " + championship.getName());
        return championshipMapper.toResponse(championship);
    }
}
