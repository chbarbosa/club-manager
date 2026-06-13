package com.clubmanager.mapper;

import com.clubmanager.domain.AuditEvent;
import com.clubmanager.dto.AuditEventResponse;
import org.springframework.stereotype.Component;

@Component
public class AuditEventMapper {

    public AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(
                event.getUuid(),
                event.getOccurredAt(),
                event.getActorAdmin() == null ? null : event.getActorAdmin().getUuid(),
                event.getActorName(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityUuid(),
                event.getEntityLabel(),
                event.getMessage());
    }
}
