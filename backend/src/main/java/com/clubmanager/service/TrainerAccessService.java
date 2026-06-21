package com.clubmanager.service;

import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.TrainerPasswordConfirmRequest;
import com.clubmanager.dto.TrainerPasswordResetConfirmRequest;
import com.clubmanager.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TrainerAccessService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_BOUND = 100_000;
    private static final int CODE_MIN = 10_000;

    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminPasswordPolicyService passwordPolicyService;
    private final AccessEmailService accessEmailService;

    @Transactional
    public Trainer inviteTrainer(UUID trainerUuid) {
        Trainer trainer = getTrainer(trainerUuid);
        requireActiveTrainerWithEmail(trainer);
        String code = generateFiveDigitCode();
        trainer.setPasswordSetupCodeHash(passwordEncoder.encode(code));
        trainer.setPasswordSetupCodeExpiresAt(LocalDateTime.now().plusMinutes(30));
        trainer.setAccessInvitedAt(LocalDateTime.now());
        trainer.setPasswordResetCodeHash(null);
        trainer.setPasswordResetCodeExpiresAt(null);
        Trainer saved = trainerRepository.save(trainer);
        accessEmailService.sendTrainerAccessCode(saved, code);
        return saved;
    }

    @Transactional
    public Trainer confirmFirstPassword(TrainerPasswordConfirmRequest request) {
        Trainer trainer = trainerRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new EntityNotFoundException("Trainer access not found"));
        requireActiveTrainerWithEmail(trainer);
        validateCode(request.code(), trainer.getPasswordSetupCodeHash(), trainer.getPasswordSetupCodeExpiresAt());
        passwordPolicyService.validate(request.password());
        trainer.setPasswordHash(passwordEncoder.encode(request.password()));
        trainer.setPasswordSetupCodeHash(null);
        trainer.setPasswordSetupCodeExpiresAt(null);
        return trainerRepository.save(trainer);
    }

    @Transactional
    public Trainer requestPasswordReset(String email) {
        Trainer trainer = trainerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));
        requireActiveTrainerWithEmail(trainer);
        String code = generateFiveDigitCode();
        trainer.setPasswordResetCodeHash(passwordEncoder.encode(code));
        trainer.setPasswordResetCodeExpiresAt(LocalDateTime.now().plusMinutes(30));
        Trainer saved = trainerRepository.save(trainer);
        accessEmailService.sendTrainerPasswordResetCode(saved, code);
        return saved;
    }

    @Transactional
    public Trainer confirmPasswordReset(String email, TrainerPasswordResetConfirmRequest request) {
        Trainer trainer = trainerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));
        requireActiveTrainerWithEmail(trainer);
        validateCode(request.code(), trainer.getPasswordResetCodeHash(), trainer.getPasswordResetCodeExpiresAt());
        passwordPolicyService.validate(request.password());
        trainer.setPasswordHash(passwordEncoder.encode(request.password()));
        trainer.setPasswordResetCodeHash(null);
        trainer.setPasswordResetCodeExpiresAt(null);
        return trainerRepository.save(trainer);
    }

    private Trainer getTrainer(UUID trainerUuid) {
        return trainerRepository.findByUuid(trainerUuid)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUuid));
    }

    private void requireActiveTrainerWithEmail(Trainer trainer) {
        if (!trainer.isActive()) {
            throw new IllegalArgumentException("Trainer must be active");
        }
        if (!StringUtils.hasText(trainer.getEmail())) {
            throw new IllegalArgumentException("Trainer email is required for access");
        }
    }

    private void validateCode(String code, String codeHash, LocalDateTime expiresAt) {
        if (!StringUtils.hasText(codeHash) || expiresAt == null) {
            throw new IllegalArgumentException("No active confirmation code");
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Confirmation code expired");
        }
        if (!passwordEncoder.matches(code, codeHash)) {
            throw new IllegalArgumentException("Invalid confirmation code");
        }
    }

    private String generateFiveDigitCode() {
        return String.valueOf(RANDOM.nextInt(CODE_BOUND - CODE_MIN) + CODE_MIN);
    }
}
