package com.clubmanager.service;

import com.clubmanager.domain.Admin;
import com.clubmanager.domain.AuditEvent;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.AuditEventRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventService.class);

    public static final String CREATED = "CREATED";
    public static final String UPDATED = "UPDATED";
    public static final String DEACTIVATED = "DEACTIVATED";
    public static final String REACTIVATED = "REACTIVATED";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String REMOVED = "REMOVED";
    public static final String CANCELED = "CANCELED";
    public static final String COMPLETED = "COMPLETED";
    public static final String STARTED = "STARTED";
    public static final String FINALIZED = "FINALIZED";
    public static final String EVALUATED = "EVALUATED";

    public static final String ADMIN = "ADMIN";
    public static final String PLAYER = "PLAYER";
    public static final String TRAINER = "TRAINER";
    public static final String TEAM = "TEAM";
    public static final String TEAM_ROSTER = "TEAM_ROSTER";
    public static final String SCHEDULE = "SCHEDULE";
    public static final String EVALUATION = "EVALUATION";
    public static final String EVALUATION_PLAYER = "EVALUATION_PLAYER";
    public static final String EVALUATION_EVENT = "EVALUATION_EVENT";
    public static final String EVALUATION_ATTENDANCE = "EVALUATION_ATTENDANCE";
    public static final String EVALUATION_RESULT = "EVALUATION_RESULT";
    public static final String CHAMPIONSHIP = "CHAMPIONSHIP";
    public static final String TEAM_MATCH = "TEAM_MATCH";
    public static final String MATCH_PLAYER_ANALYSIS = "MATCH_PLAYER_ANALYSIS";

    private final AuditEventRepository auditEventRepository;
    private final AdminRepository adminRepository;
    private final AppMetricsService appMetricsService;



    @Transactional
    public AuditEvent record(String action, String entityType, UUID entityUuid, String entityLabel, String message) {
        Admin actor = getCurrentAdmin();
        AuditEvent auditEvent = auditEventRepository.save(AuditEvent.builder()
                .occurredAt(LocalDateTime.now())
                .actorAdmin(actor)
                .actorName(actor.getName())
                .action(action)
                .entityType(entityType)
                .entityUuid(entityUuid)
                .entityLabel(clean(entityLabel))
                .message(clean(message))
                .build());
        appMetricsService.recordAuditEventRecorded();
        LOGGER.info("Audit event recorded action={} entityType={} entityUuid={}", action, entityType, entityUuid);
        return auditEvent;
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> search(
            String entityType,
            String action,
            UUID adminUuid,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {
        Admin actor = adminUuid == null ? null : adminRepository.findByUuid(adminUuid)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found: " + adminUuid));
        return auditEventRepository.findAll(specification(entityType, action, actor, from, to), pageable);
    }

    private Specification<AuditEvent> specification(
            String entityType,
            String action,
            Admin actor,
            LocalDateTime from,
            LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            if (StringUtils.hasText(entityType)) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType.trim()));
            }
            if (StringUtils.hasText(action)) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action.trim()));
            }
            if (actor != null) {
                predicates.add(criteriaBuilder.equal(root.get("actorAdmin"), actor));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("occurredAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("occurredAt"), to));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Admin getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new IllegalStateException("Authenticated admin is required to record audit events");
        }
        return adminRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated admin not found: "
                        + authentication.getName()));
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
