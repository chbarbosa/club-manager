package com.clubmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
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
public class Evaluation extends AbstractEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EvaluationStatus status = EvaluationStatus.OPEN;

    @Column(nullable = false, length = 50)
    private String ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamCategory teamCategory;

    @Column(nullable = false, updatable = false)
    private LocalDate createdDate;
}
