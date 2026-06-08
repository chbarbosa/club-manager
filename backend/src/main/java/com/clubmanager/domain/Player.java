package com.clubmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
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
public class Player extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String birthCountry;

    @Column(nullable = false, length = 100)
    private String livingCountry;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamCategory teamCategory;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SkillLevel currentSkillLevel;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_position", joinColumns = @JoinColumn(name = "player_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 20)
    private Set<PlayerPosition> positions = new LinkedHashSet<>();

    @Column(length = 50)
    private String registrationNumber;

    @Column(nullable = false, updatable = false)
    private LocalDate registerDate;

    @Column(nullable = false)
    private LocalDate memberSince;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
