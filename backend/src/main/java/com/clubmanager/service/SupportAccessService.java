package com.clubmanager.service;

import com.clubmanager.domain.Admin;
import com.clubmanager.domain.SupportAccess;
import com.clubmanager.domain.SupportAccessViewEvent;
import com.clubmanager.dto.SupportAccessCreateRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.SupportAccessRepository;
import com.clubmanager.repository.SupportAccessViewEventRepository;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupportAccessService {

    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SupportAccessRepository supportAccessRepository;
    private final SupportAccessViewEventRepository viewEventRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessEmailService accessEmailService;

    @Transactional
    public SupportAccess createSupportAccess(SupportAccessCreateRequest request) {
        Admin admin = currentAdmin();
        String password = randomPassword();
        LocalDateTime now = LocalDateTime.now();
        SupportAccess supportAccess = supportAccessRepository.save(SupportAccess.builder()
                .email(request.email().trim())
                .passwordHash(passwordEncoder.encode(password))
                .createdAt(now)
                .expiresAt(now.plusHours(5))
                .createdByAdmin(admin)
                .build());
        accessEmailService.sendSupportAccessPassword(supportAccess.getEmail(), password);
        return supportAccess;
    }

    @Transactional(readOnly = true)
    public Page<SupportAccess> getSupportAccesses(Pageable pageable) {
        return supportAccessRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public SupportAccess getSupportAccess(UUID uuid) {
        return supportAccessRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Support access not found: " + uuid));
    }

    @Transactional
    public SupportAccess revokeSupportAccess(UUID uuid) {
        SupportAccess supportAccess = getSupportAccess(uuid);
        if (supportAccess.getRevokedAt() == null) {
            supportAccess.setRevokedAt(LocalDateTime.now());
        }
        return supportAccessRepository.save(supportAccess);
    }

    @Transactional(readOnly = true)
    public Page<SupportAccessViewEvent> getViewEvents(UUID supportAccessUuid, Pageable pageable) {
        SupportAccess supportAccess = getSupportAccess(supportAccessUuid);
        return viewEventRepository.findAllBySupportAccessOrderByOccurredAtDesc(supportAccess, pageable);
    }

    private Admin currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authenticated admin is required");
        }
        return adminRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated admin not found: " + authentication.getName()));
    }

    private String randomPassword() {
        StringBuilder builder = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            builder.append(PASSWORD_ALPHABET.charAt(RANDOM.nextInt(PASSWORD_ALPHABET.length())));
        }
        return builder.toString();
    }

}
