package com.clubmanager.service;

import com.clubmanager.config.SupportAccessConfig;
import com.clubmanager.domain.Admin;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.AuthenticatedUser;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.SupportAccessRepository;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLoginService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TRAINER = "TRAINER";
    public static final String ROLE_SUPPORT = "SUPPORT";

    private final AdminRepository adminRepository;
    private final TrainerRepository trainerRepository;
    private final SupportAccessRepository supportAccessRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;
    private final SupportAccessConfig supportAccessConfig;

    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(LoginRequest request, String clientAddress) {
        loginRateLimiter.ensureAllowed(request.username(), clientAddress);

        List<LoginCandidate> candidates = new ArrayList<>();

        adminRepository.findByUsername(request.username())
                .filter(admin -> isValidAdmin(admin, request.password()))
                .ifPresent(admin -> candidates.add(new LoginCandidate(
                        admin.getUsername(),
                        admin.getUuid(),
                        admin.getName(),
                        ROLE_ADMIN)));

        trainerRepository.findByEmailIgnoreCase(request.username())
                .filter(trainer -> isValidTrainer(trainer, request.password()))
                .ifPresent(trainer -> candidates.add(new LoginCandidate(
                        trainer.getEmail(),
                        trainer.getUuid(),
                        trainer.getName(),
                        ROLE_TRAINER)));

        if (supportAccessConfig.enabled()) {
            supportAccessRepository.findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(request.username())
                    .filter(supportAccess -> supportAccess.isActive(LocalDateTime.now()))
                    .filter(supportAccess -> passwordEncoder.matches(request.password(), supportAccess.getPasswordHash()))
                    .ifPresent(supportAccess -> candidates.add(new LoginCandidate(
                            supportAccess.getEmail(),
                            supportAccess.getUuid(),
                            "Support",
                            ROLE_SUPPORT)));
        }

        if (!candidates.isEmpty()) {
            loginRateLimiter.recordSuccess(request.username(), clientAddress);
            LoginCandidate effectiveUser = candidates.get(0);
            return new AuthenticatedUser(
                    effectiveUser.username(),
                    effectiveUser.uuid(),
                    effectiveUser.name(),
                    effectiveUser.role(),
                    candidates.stream().map(LoginCandidate::role).toList());
        }

        loginRateLimiter.recordFailure(request.username(), clientAddress);
        throw new BadCredentialsException("Invalid username or password");
    }

    private boolean isValidAdmin(Admin admin, String password) {
        return admin.isActive() && passwordEncoder.matches(password, admin.getPasswordHash());
    }

    private boolean isValidTrainer(Trainer trainer, String password) {
        return trainer.isActive()
                && trainer.getPasswordHash() != null
                && passwordEncoder.matches(password, trainer.getPasswordHash());
    }

    private record LoginCandidate(String username, UUID uuid, String name, String role) {
    }
}
