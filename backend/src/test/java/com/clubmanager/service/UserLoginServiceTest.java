package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Admin;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDate;
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
    private LoginRateLimiter loginRateLimiter;

    private PasswordEncoder passwordEncoder;
    private UserLoginService userLoginService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userLoginService = new UserLoginService(adminRepository, trainerRepository, passwordEncoder, loginRateLimiter);
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

    private Trainer trainer() {
        return Trainer.builder()
                .name("Carlos Mendes")
                .email("carlos@club.com")
                .registerDate(LocalDate.now().minusDays(10))
                .memberSince(LocalDate.now().minusYears(5))
                .build();
    }
}
