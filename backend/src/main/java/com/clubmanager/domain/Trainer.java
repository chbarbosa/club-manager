package com.clubmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class Trainer extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String birthCountry;

    @Column(length = 100)
    private String livingCountry;

    private LocalDate birthdate;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String passwordSetupCodeHash;

    private LocalDateTime passwordSetupCodeExpiresAt;

    @Column(length = 255)
    private String passwordResetCodeHash;

    private LocalDateTime passwordResetCodeExpiresAt;

    private LocalDateTime accessInvitedAt;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, updatable = false)
    private LocalDate registerDate;

    @Column(nullable = false)
    private LocalDate memberSince;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
