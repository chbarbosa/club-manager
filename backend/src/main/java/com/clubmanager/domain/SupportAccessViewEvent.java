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
public class SupportAccessViewEvent extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "support_access_id", nullable = false)
    private SupportAccess supportAccess;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, length = 100)
    private String feature;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false, length = 500)
    private String path;

    private UUID entityUuid;
}
