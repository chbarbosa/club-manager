package com.clubmanager.controller;

import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.SupportAccessCreateRequest;
import com.clubmanager.dto.SupportAccessResponse;
import com.clubmanager.dto.SupportAccessViewEventResponse;
import com.clubmanager.mapper.SupportAccessMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.SupportAccessService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support-access")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SupportAccessController {

    private final SupportAccessService supportAccessService;
    private final SupportAccessMapper supportAccessMapper;
    private final AuditEventService auditEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportAccessResponse createSupportAccess(@Valid @RequestBody SupportAccessCreateRequest request) {
        var supportAccess = supportAccessService.createSupportAccess(request);
        auditEventService.record(
                AuditEventService.CREATED,
                "SUPPORT_ACCESS",
                supportAccess.getUuid(),
                supportAccess.getEmail(),
                "Support access created for: " + supportAccess.getEmail());
        return supportAccessMapper.toResponse(supportAccess);
    }

    @GetMapping
    public PageResponse<SupportAccessResponse> getSupportAccesses(Pageable pageable) {
        return PageResponse.from(supportAccessService.getSupportAccesses(pageable)
                .map(supportAccessMapper::toResponse));
    }

    @GetMapping("/{uuid}")
    public SupportAccessResponse getSupportAccess(@PathVariable UUID uuid) {
        return supportAccessMapper.toResponse(supportAccessService.getSupportAccess(uuid));
    }

    @PatchMapping("/{uuid}/revoke")
    public SupportAccessResponse revokeSupportAccess(@PathVariable UUID uuid) {
        var supportAccess = supportAccessService.revokeSupportAccess(uuid);
        auditEventService.record(
                AuditEventService.DEACTIVATED,
                "SUPPORT_ACCESS",
                supportAccess.getUuid(),
                supportAccess.getEmail(),
                "Support access revoked for: " + supportAccess.getEmail());
        return supportAccessMapper.toResponse(supportAccess);
    }

    @GetMapping("/{uuid}/views")
    public PageResponse<SupportAccessViewEventResponse> getViewEvents(@PathVariable UUID uuid, Pageable pageable) {
        return PageResponse.from(supportAccessService.getViewEvents(uuid, pageable)
                .map(supportAccessMapper::toResponse));
    }
}
