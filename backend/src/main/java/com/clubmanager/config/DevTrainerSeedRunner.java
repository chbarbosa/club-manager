package com.clubmanager.config;

import com.clubmanager.domain.Trainer;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevTrainerSeedRunner implements ApplicationRunner {

    static final String DEV_TRAINER_EMAIL = "trainer@clubmanager.com";
    static final String DEV_TRAINER_PASSWORD = "pass123";

    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        var existingTrainer = trainerRepository.findByEmailIgnoreCase(DEV_TRAINER_EMAIL);
        if (existingTrainer.isPresent()) {
            Trainer trainer = existingTrainer.get();
            trainer.setActive(true);
            trainer.setPasswordHash(passwordEncoder.encode(DEV_TRAINER_PASSWORD));
            trainerRepository.save(trainer);
            return;
        }

        trainerRepository.save(Trainer.builder()
                .name("Dev Trainer")
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.of(1988, 4, 20))
                .email(DEV_TRAINER_EMAIL)
                .phone("555-0200")
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .active(true)
                .passwordHash(passwordEncoder.encode(DEV_TRAINER_PASSWORD))
                .build());
    }
}
