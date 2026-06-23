package com.clubmanager.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Trainer;
import com.clubmanager.repository.TrainerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class DevTrainerSeedRunnerTest {

    private TrainerRepository trainerRepository;
    private PasswordEncoder passwordEncoder;
    private DevTrainerSeedRunner runner;

    @BeforeEach
    void setUp() {
        trainerRepository = org.mockito.Mockito.mock(TrainerRepository.class);
        passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        runner = new DevTrainerSeedRunner(trainerRepository, passwordEncoder);
    }

    @Test
    void run_WhenDevTrainerDoesNotExist_CreatesActiveTrainerWithDefaultPassword() {
        when(trainerRepository.findByEmailIgnoreCase(DevTrainerSeedRunner.DEV_TRAINER_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(DevTrainerSeedRunner.DEV_TRAINER_PASSWORD)).thenReturn("encoded-password");

        runner.run(null);

        ArgumentCaptor<Trainer> trainer = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(trainer.capture());
        assertThat(trainer.getValue().getName()).isEqualTo("Dev Trainer");
        assertThat(trainer.getValue().getEmail()).isEqualTo(DevTrainerSeedRunner.DEV_TRAINER_EMAIL);
        assertThat(trainer.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(trainer.getValue().isActive()).isTrue();
    }

    @Test
    void run_WhenDevTrainerExists_DoesNotOverwriteIt() {
        when(trainerRepository.findByEmailIgnoreCase(DevTrainerSeedRunner.DEV_TRAINER_EMAIL))
                .thenReturn(Optional.of(Trainer.builder().name("Existing").build()));

        runner.run(null);

        verify(trainerRepository, never()).save(any());
    }
}
