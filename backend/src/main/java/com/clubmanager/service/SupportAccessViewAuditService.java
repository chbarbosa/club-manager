package com.clubmanager.service;

import com.clubmanager.domain.SupportAccessViewEvent;
import com.clubmanager.repository.SupportAccessRepository;
import com.clubmanager.repository.SupportAccessViewEventRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupportAccessViewAuditService {

    private final SupportAccessRepository supportAccessRepository;
    private final SupportAccessViewEventRepository viewEventRepository;

    @Transactional
    public void recordView(String email, String method, String path) {
        supportAccessRepository.findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .filter(access -> access.isActive(LocalDateTime.now()))
                .ifPresent(access -> viewEventRepository.save(SupportAccessViewEvent.builder()
                        .supportAccess(access)
                        .occurredAt(LocalDateTime.now())
                        .feature(feature(path))
                        .httpMethod(method)
                        .path(path)
                        .entityUuid(firstUuid(path))
                        .build()));
    }

    private String feature(String path) {
        String prefix = "/api/v1/";
        if (!path.startsWith(prefix)) {
            return path;
        }
        String remaining = path.substring(prefix.length());
        int slash = remaining.indexOf('/');
        return slash == -1 ? remaining : remaining.substring(0, slash);
    }

    private UUID firstUuid(String path) {
        for (String segment : path.split("/")) {
            try {
                return UUID.fromString(segment);
            } catch (IllegalArgumentException ignored) {
                // Keep scanning path segments.
            }
        }
        return null;
    }
}
