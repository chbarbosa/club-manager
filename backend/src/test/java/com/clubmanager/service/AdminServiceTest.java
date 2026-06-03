package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Admin;
import com.clubmanager.dto.AdminRegisterRequest;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.repository.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    private PasswordEncoder passwordEncoder;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        adminService = new AdminService(adminRepository, passwordEncoder);
    }

    @Test
    void register_WithValidRequest_ReturnsAdmin() {
        AdminRegisterRequest request = new AdminRegisterRequest("Jane Admin", "jane@club.com", "jane", "secret1");
        when(adminRepository.existsByUsername("jane")).thenReturn(false);
        when(adminRepository.existsByEmail("jane@club.com")).thenReturn(false);
        when(adminRepository.save(org.mockito.ArgumentMatchers.any(Admin.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Admin admin = adminService.register(request);

        assertThat(admin.getName()).isEqualTo("Jane Admin");
        assertThat(admin.getPasswordHash()).isNotEqualTo("secret1");
        assertThat(passwordEncoder.matches("secret1", admin.getPasswordHash())).isTrue();
    }

    @Test
    void register_WithDuplicateUsername_ThrowsException() {
        AdminRegisterRequest request = new AdminRegisterRequest("Jane Admin", "jane@club.com", "jane", "secret1");
        when(adminRepository.existsByUsername("jane")).thenReturn(true);

        assertThatThrownBy(() -> adminService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_WithDuplicateEmail_ThrowsException() {
        AdminRegisterRequest request = new AdminRegisterRequest("Jane Admin", "jane@club.com", "jane", "secret1");
        when(adminRepository.existsByUsername("jane")).thenReturn(false);
        when(adminRepository.existsByEmail("jane@club.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void authenticate_WithValidCredentials_ReturnsAdmin() {
        Admin admin = admin("admin", "admin123");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThat(adminService.authenticate(new LoginRequest("admin", "admin123"))).isEqualTo(admin);
    }

    @Test
    void authenticate_WithInvalidPassword_ThrowsException() {
        Admin admin = admin("admin", "admin123");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminService.authenticate(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void authenticate_WhenAdminInactive_ThrowsException() {
        Admin admin = admin("admin", "admin123");
        admin.setActive(false);
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminService.authenticate(new LoginRequest("admin", "admin123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deactivateAdmin_WhenOnlyOneActiveAdmin_ThrowsException() {
        Admin admin = admin("admin", "admin123");
        when(adminRepository.findByUuid(admin.getUuid())).thenReturn(Optional.of(admin));
        when(adminRepository.countByActiveTrue()).thenReturn(1L);

        assertThatThrownBy(() -> adminService.deactivateAdmin(admin.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("last active admin");
    }

    @Test
    void deactivateAdmin_WhenMultipleActiveAdmins_SetsInactive() {
        Admin admin = admin("jane", "secret1");
        when(adminRepository.findByUuid(admin.getUuid())).thenReturn(Optional.of(admin));
        when(adminRepository.countByActiveTrue()).thenReturn(2L);
        when(adminRepository.save(admin)).thenReturn(admin);

        Admin updated = adminService.deactivateAdmin(admin.getUuid());

        assertThat(updated.isActive()).isFalse();
        verify(adminRepository).save(admin);
        verify(adminRepository, never()).delete(admin);
    }

    @Test
    void reactivateAdmin_WhenInactive_SetsActive() {
        Admin admin = admin("jane", "secret1");
        admin.setActive(false);
        when(adminRepository.findByUuid(admin.getUuid())).thenReturn(Optional.of(admin));
        when(adminRepository.save(admin)).thenReturn(admin);

        Admin updated = adminService.reactivateAdmin(admin.getUuid());

        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void getAdminByUuid_WhenMissing_ThrowsNotFound() {
        UUID uuid = UUID.randomUUID();
        when(adminRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getAdminByUuid(uuid))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Admin admin(String username, String password) {
        Admin admin = new Admin();
        admin.setName("Admin");
        admin.setEmail(username + "@club.com");
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        return admin;
    }
}
