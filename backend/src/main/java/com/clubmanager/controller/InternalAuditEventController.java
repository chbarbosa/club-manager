package com.clubmanager.controller;

import com.clubmanager.dto.AuditEventResponse;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.mapper.AuditEventMapper;
import com.clubmanager.service.AuditEventService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/api/v1/audit-events")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class InternalAuditEventController {

    private final AuditEventService auditEventService;
    private final AuditEventMapper auditEventMapper;



    @GetMapping
    public PageResponse<AuditEventResponse> getAuditEvents(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID adminUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return PageResponse.from(auditEventService.search(entityType, action, adminUuid, from, to, pageable)
                .map(auditEventMapper::toResponse));
    }
}
