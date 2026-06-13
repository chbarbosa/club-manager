package com.clubmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
@Entity
public class AuditEvent extends AbstractEntity {

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_admin_id")
    private Admin actorAdmin;

    @Column(length = 100)
    private String actorName;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(nullable = false, length = 60)
    private String entityType;

    @Column(nullable = false)
    private UUID entityUuid;

    @Column(length = 255)
    private String entityLabel;

    @Column(length = 500)
    private String message;
}
