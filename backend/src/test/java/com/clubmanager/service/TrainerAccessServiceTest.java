package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.config.AppSecurityConfig;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.TrainerPasswordConfirmRequest;
import com.clubmanager.dto.TrainerPasswordResetConfirmRequest;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TrainerAccessServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private AccessEmailService accessEmailService;

    private PasswordEncoder passwordEncoder;
    private TrainerAccessService trainerAccessService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        var securityConfig = new AppSecurityConfig(
                new AppSecurityConfig.PasswordPolicy(10),
                new AppSecurityConfig.LoginRateLimit(true, 5, 15, "in-memory"));
        trainerAccessService = new TrainerAccessService(
                trainerRepository,
                passwordEncoder,
                new AdminPasswordPolicyService(securityConfig),
                accessEmailService);
    }

    @Test
    void inviteTrainer_WithActiveTrainer_SetsSetupCodeAndSendsEmail() {
        Trainer trainer = trainer();
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer updated = trainerAccessService.inviteTrainer(trainer.getUuid());

        assertThat(updated.getPasswordSetupCodeHash()).isNotBlank();
        assertThat(updated.getPasswordSetupCodeExpiresAt()).isNotNull();
        assertThat(updated.getAccessInvitedAt()).isNotNull();
        verify(accessEmailService).sendTrainerAccessCode(same(trainer), anyString());
    }

    @Test
    void inviteTrainer_WithInactiveTrainer_Fails() {
        Trainer trainer = trainer();
        trainer.setActive(false);
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerAccessService.inviteTrainer(trainer.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("active");
    }

    @Test
    void confirmFirstPassword_WithValidCode_SetsPasswordHashAndClearsCode() {
        Trainer trainer = trainer();
        String code = "12345";
        trainer.setPasswordSetupCodeHash(passwordEncoder.encode(code));
        trainer.setPasswordSetupCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));
        when(trainerRepository.findByEmailIgnoreCase(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer updated = trainerAccessService.confirmFirstPassword(
                new TrainerPasswordConfirmRequest(trainer.getEmail(), code, "StrongPass1"));

        assertThat(passwordEncoder.matches("StrongPass1", updated.getPasswordHash())).isTrue();
        assertThat(updated.getPasswordSetupCodeHash()).isNull();
        assertThat(updated.getPasswordSetupCodeExpiresAt()).isNull();
    }

    @Test
    void confirmFirstPassword_WithInvalidCode_Fails() {
        Trainer trainer = trainer();
        trainer.setPasswordSetupCodeHash(passwordEncoder.encode("12345"));
        trainer.setPasswordSetupCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));
        when(trainerRepository.findByEmailIgnoreCase(trainer.getEmail())).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerAccessService.confirmFirstPassword(
                new TrainerPasswordConfirmRequest(trainer.getEmail(), "99999", "StrongPass1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid confirmation code");
    }

    @Test
    void passwordReset_WithValidCode_UpdatesPassword() {
        Trainer trainer = trainer();
        String code = "54321";
        trainer.setPasswordHash(passwordEncoder.encode("OldStrong1"));
        trainer.setPasswordResetCodeHash(passwordEncoder.encode(code));
        trainer.setPasswordResetCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));
        when(trainerRepository.findByEmailIgnoreCase(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer updated = trainerAccessService.confirmPasswordReset(
                trainer.getEmail(),
                new TrainerPasswordResetConfirmRequest(code, "NewStrong1"));

        assertThat(passwordEncoder.matches("NewStrong1", updated.getPasswordHash())).isTrue();
        assertThat(updated.getPasswordResetCodeHash()).isNull();
    }

    private Trainer trainer() {
        return Trainer.builder()
                .name("Carlos Mendes")
                .email("carlos@club.com")
                .registerDate(LocalDate.now().minusDays(10))
                .memberSince(LocalDate.now().minusYears(5))
                .build();
    }
}
