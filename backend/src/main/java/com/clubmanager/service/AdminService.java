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

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Admin register(AdminRegisterRequest request) {
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
        Admin admin = adminRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!admin.isActive() || !passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return admin;
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
