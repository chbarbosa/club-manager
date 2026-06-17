package com.clubmanager.service;

import com.clubmanager.domain.Admin;
import com.clubmanager.dto.AdminRegisterRequest;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.repository.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminPasswordPolicyService adminPasswordPolicyService;
    private final LoginRateLimiter loginRateLimiter;



    @Transactional
    public Admin register(AdminRegisterRequest request) {
        adminPasswordPolicyService.validate(request.password());
        if (adminRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (adminRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setUsername(request.username());
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        return adminRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public Admin authenticate(LoginRequest request) {
        return authenticate(request, "unknown");
    }

    @Transactional(readOnly = true)
    public Admin authenticate(LoginRequest request, String clientAddress) {
        loginRateLimiter.ensureAllowed(request.username(), clientAddress);
        Admin admin = adminRepository.findByUsername(request.username())
                .orElseThrow(() -> badCredentials(request.username(), clientAddress));
        if (!admin.isActive() || !passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw badCredentials(request.username(), clientAddress);
        }
        loginRateLimiter.recordSuccess(request.username(), clientAddress);
        return admin;
    }

    private BadCredentialsException badCredentials(String username, String clientAddress) {
        loginRateLimiter.recordFailure(username, clientAddress);
        return new BadCredentialsException("Invalid username or password");
    }

    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Admin> getActiveAdmins() {
        return adminRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Admin getAdminByUuid(UUID uuid) {
        return adminRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found: " + uuid));
    }

    @Transactional
    public Admin deactivateAdmin(UUID uuid) {
        Admin admin = getAdminByUuid(uuid);
        if (!admin.isActive()) {
            return admin;
        }
        if (adminRepository.countByActiveTrue() <= 1) {
            throw new IllegalArgumentException("Cannot deactivate the last active admin");
        }
        admin.setActive(false);
        return adminRepository.save(admin);
    }

    @Transactional
    public Admin reactivateAdmin(UUID uuid) {
        Admin admin = getAdminByUuid(uuid);
        admin.setActive(true);
        return adminRepository.save(admin);
    }
}
