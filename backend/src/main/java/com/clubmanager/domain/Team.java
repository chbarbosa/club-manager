package com.clubmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Team extends AbstractEntity {

    @Column(nullable = false, length = 50)
    private String ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamAgeCategory ageCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamCategory teamCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_trainer_id")
    private Trainer subTrainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_admin_id")
    private Admin assistantAdmin;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
