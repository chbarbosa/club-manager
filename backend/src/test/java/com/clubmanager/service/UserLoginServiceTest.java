package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.config.SupportAccessConfig;
import com.clubmanager.domain.Admin;
import com.clubmanager.domain.SupportAccess;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.SupportAccessRepository;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private SupportAccessRepository supportAccessRepository;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    private PasswordEncoder passwordEncoder;
    private UserLoginService userLoginService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userLoginService = new UserLoginService(
                adminRepository,
                trainerRepository,
                supportAccessRepository,
                passwordEncoder,
                loginRateLimiter,
                new SupportAccessConfig(true));
    }

    @Test
    void authenticate_WithTrainerEmailAndPassword_ReturnsTrainerUser() {
        Trainer trainer = trainer();
        trainer.setPasswordHash(passwordEncoder.encode("StrongPass1"));
        when(adminRepository.findByUsername(trainer.getEmail())).thenReturn(Optional.empty());
        when(trainerRepository.findByEmailIgnoreCase(trainer.getEmail())).thenReturn(Optional.of(trainer));

        var user = userLoginService.authenticate(new LoginRequest(trainer.getEmail(), "StrongPass1"), "127.0.0.1");

        assertThat(user.role()).isEqualTo(UserLoginService.ROLE_TRAINER);
        assertThat(user.username()).isEqualTo(trainer.getEmail());
        verify(loginRateLimiter).recordSuccess(trainer.getEmail(), "127.0.0.1");
    }

    @Test
    void authenticate_WithInactiveTrainer_Fails() {
        Trainer trainer = trainer();
        trainer.setActive(false);
        trainer.setPasswordHash(passwordEncoder.encode("StrongPass1"));
        when(adminRepository.findByUsername(trainer.getEmail())).thenReturn(Optional.empty());
        when(trainerRepository.findByEmailIgnoreCase(trainer.getEmail())).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> userLoginService.authenticate(new LoginRequest(trainer.getEmail(), "StrongPass1"), "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);
        verify(loginRateLimiter).recordFailure(trainer.getEmail(), "127.0.0.1");
    }

    @Test
    void authenticate_WithAdminUsername_ReturnsAdminUser() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setName("Admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        var user = userLoginService.authenticate(new LoginRequest("admin", "admin123"), "127.0.0.1");

        assertThat(user.role()).isEqualTo(UserLoginService.ROLE_ADMIN);
    }

    @Test
    void authenticate_WithAdminAndTrainerCredentials_UsesAdminPriorityAndReportsBothRoles() {
        Admin admin = new Admin();
        admin.setUsername("carlos@club.com");
        admin.setName("Carlos Admin");
        admin.setPasswordHash(passwordEncoder.encode("StrongPass1"));

        Trainer trainer = trainer();
        trainer.setPasswordHash(passwordEncoder.encode("StrongPass1"));

        when(adminRepository.findByUsername("carlos@club.com")).thenReturn(Optional.of(admin));
        when(trainerRepository.findByEmailIgnoreCase("carlos@club.com")).thenReturn(Optional.of(trainer));

        var user = userLoginService.authenticate(new LoginRequest("carlos@club.com", "StrongPass1"), "127.0.0.1");

        assertThat(user.role()).isEqualTo(UserLoginService.ROLE_ADMIN);
        assertThat(user.availableRoles()).containsExactly(UserLoginService.ROLE_ADMIN, UserLoginService.ROLE_TRAINER);
        assertThat(user.multipleRoles()).isTrue();
    }

    @Test
    void authenticate_WithActiveSupportAccess_ReturnsSupportUser() {
        SupportAccess supportAccess = SupportAccess.builder()
                .email("support@example.com")
                .passwordHash(passwordEncoder.encode("Support123"))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(5))
                .createdByAdmin(new Admin())
                .build();
        when(adminRepository.findByUsername("support@example.com")).thenReturn(Optional.empty());
        when(trainerRepository.findByEmailIgnoreCase("support@example.com")).thenReturn(Optional.empty());
        when(supportAccessRepository.findFirstByEmailIgnoreCaseOrderByCreatedAtDesc("support@example.com"))
                .thenReturn(Optional.of(supportAccess));

        var user = userLoginService.authenticate(new LoginRequest("support@example.com", "Support123"), "127.0.0.1");

        assertThat(user.role()).isEqualTo(UserLoginService.ROLE_SUPPORT);
        assertThat(user.username()).isEqualTo("support@example.com");
    }

    @Test
    void authenticate_WithSupportAccessDisabled_IgnoresSupportCredentials() {
        UserLoginService disabledSupportLoginService = new UserLoginService(
                adminRepository,
                trainerRepository,
                supportAccessRepository,
                passwordEncoder,
                loginRateLimiter,
                new SupportAccessConfig(false));
        when(adminRepository.findByUsername("support@example.com")).thenReturn(Optional.empty());
        when(trainerRepository.findByEmailIgnoreCase("support@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disabledSupportLoginService.authenticate(
                new LoginRequest("support@example.com", "Support123"),
                "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        verify(supportAccessRepository, never()).findFirstByEmailIgnoreCaseOrderByCreatedAtDesc("support@example.com");
        verify(loginRateLimiter).recordFailure("support@example.com", "127.0.0.1");
    }

    @Test
    void authenticate_WithTrainerAndSupportCredentials_UsesTrainerPriorityAndReportsBothRoles() {
        Trainer trainer = trainer();
        trainer.setPasswordHash(passwordEncoder.encode("StrongPass1"));

        SupportAccess supportAccess = SupportAccess.builder()
                .email(trainer.getEmail())
                .passwordHash(passwordEncoder.encode("StrongPass1"))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(5))
                .createdByAdmin(new Admin())
                .build();

        when(adminRepository.findByUsername(trainer.getEmail())).thenReturn(Optional.empty());
        when(trainerRepository.findByEmailIgnoreCase(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(supportAccessRepository.findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(trainer.getEmail()))
                .thenReturn(Optional.of(supportAccess));

        var user = userLoginService.authenticate(new LoginRequest(trainer.getEmail(), "StrongPass1"), "127.0.0.1");

        assertThat(user.role()).isEqualTo(UserLoginService.ROLE_TRAINER);
        assertThat(user.availableRoles()).containsExactly(UserLoginService.ROLE_TRAINER, UserLoginService.ROLE_SUPPORT);
        assertThat(user.multipleRoles()).isTrue();
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
