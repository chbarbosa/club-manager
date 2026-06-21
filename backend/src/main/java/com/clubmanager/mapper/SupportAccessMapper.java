package com.clubmanager.mapper;

import com.clubmanager.domain.SupportAccess;
import com.clubmanager.domain.SupportAccessViewEvent;
import com.clubmanager.dto.SupportAccessResponse;
import com.clubmanager.dto.SupportAccessViewEventResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SupportAccessMapper {

    public SupportAccessResponse toResponse(SupportAccess supportAccess) {
        return new SupportAccessResponse(
                supportAccess.getUuid(),
                supportAccess.getEmail(),
                supportAccess.getCreatedAt(),
                supportAccess.getExpiresAt(),
                supportAccess.getRevokedAt(),
                status(supportAccess),
                supportAccess.getCreatedByAdmin().getUuid(),
                supportAccess.getCreatedByAdmin().getName());
    }

    public SupportAccessViewEventResponse toResponse(SupportAccessViewEvent event) {
        return new SupportAccessViewEventResponse(
                event.getUuid(),
                event.getOccurredAt(),
                event.getFeature(),
                event.getHttpMethod(),
                event.getPath(),
                event.getEntityUuid());
    }

    private String status(SupportAccess supportAccess) {
        if (supportAccess.getRevokedAt() != null) {
            return "REVOKED";
        }
        return supportAccess.getExpiresAt().isAfter(LocalDateTime.now()) ? "ACTIVE" : "EXPIRED";
    }
}
