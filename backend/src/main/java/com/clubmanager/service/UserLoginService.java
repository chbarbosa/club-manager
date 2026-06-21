package com.clubmanager.service;

import com.clubmanager.domain.Admin;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.AuthenticatedUser;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.SupportAccessRepository;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(LoginRequest request, String clientAddress) {
        loginRateLimiter.ensureAllowed(request.username(), clientAddress);

        var admin = adminRepository.findByUsername(request.username());
        if (admin.isPresent() && isValidAdmin(admin.get(), request.password())) {
            loginRateLimiter.recordSuccess(request.username(), clientAddress);
            return new AuthenticatedUser(admin.get().getUsername(), admin.get().getUuid(), admin.get().getName(), ROLE_ADMIN);
        }

        var trainer = trainerRepository.findByEmailIgnoreCase(request.username());
        if (trainer.isPresent() && isValidTrainer(trainer.get(), request.password())) {
            loginRateLimiter.recordSuccess(request.username(), clientAddress);
            return new AuthenticatedUser(trainer.get().getEmail(), trainer.get().getUuid(), trainer.get().getName(), ROLE_TRAINER);
        }

        var supportAccess = supportAccessRepository.findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(request.username());
        if (supportAccess.isPresent()
                && supportAccess.get().isActive(LocalDateTime.now())
                && passwordEncoder.matches(request.password(), supportAccess.get().getPasswordHash())) {
            loginRateLimiter.recordSuccess(request.username(), clientAddress);
            return new AuthenticatedUser(
                    supportAccess.get().getEmail(),
                    supportAccess.get().getUuid(),
                    "Support",
                    ROLE_SUPPORT);
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
}
