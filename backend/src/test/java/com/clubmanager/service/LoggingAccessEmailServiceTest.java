package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.clubmanager.domain.Trainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(OutputCaptureExtension.class)
class LoggingAccessEmailServiceTest {

    @Test
    void sendTrainerAccessCode_WithDevProfile_LogsCode(CapturedOutput output) {
        var service = new LoggingAccessEmailService(new MockEnvironment().withProperty("spring.profiles.active", "dev"));

        service.sendTrainerAccessCode(trainer(), "12345");

        assertThat(output).contains("email=trainer@example.com");
        assertThat(output).contains("devAccessCode=12345");
    }

    @Test
    void sendTrainerAccessCode_WithoutDevProfile_DoesNotLogCode(CapturedOutput output) {
        var service = new LoggingAccessEmailService(new MockEnvironment().withProperty("spring.profiles.active", "prod"));

        service.sendTrainerAccessCode(trainer(), "12345");

        assertThat(output).contains("email=trainer@example.com");
        assertThat(output).doesNotContain("12345");
    }

    private Trainer trainer() {
        return Trainer.builder()
                .name("Trainer")
                .email("trainer@example.com")
                .active(true)
                .build();
    }
}
