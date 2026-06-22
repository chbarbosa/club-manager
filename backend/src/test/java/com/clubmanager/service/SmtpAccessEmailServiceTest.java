package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.clubmanager.config.MailConfigProperties;
import com.clubmanager.domain.Trainer;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SmtpAccessEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpAccessEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new SmtpAccessEmailService(
                mailSender,
                new MailConfigProperties(true, "no-reply@example.com", "https://club.example.com"));
    }

    @Test
    void sendTrainerAccessCode_SendsConfirmationEmail() {
        emailService.sendTrainerAccessCode(trainer(), "12345");

        SimpleMailMessage message = capturedMessage();
        assertThat(message.getFrom()).isEqualTo("no-reply@example.com");
        assertThat(message.getTo()).containsExactly("trainer@example.com");
        assertThat(message.getSubject()).isEqualTo("Club Manager trainer access");
        assertThat(message.getText()).contains("12345", "https://club.example.com/trainer-password/confirm");
    }

    @Test
    void sendSupportAccessPassword_SendsTemporaryPasswordEmail() {
        emailService.sendSupportAccessPassword("support@example.com", "AbC123xyZ9");

        SimpleMailMessage message = capturedMessage();
        assertThat(message.getTo()).containsExactly("support@example.com");
        assertThat(message.getSubject()).isEqualTo("Club Manager support access");
        assertThat(message.getText()).contains("AbC123xyZ9", "read-only", "https://club.example.com/login");
    }

    private SimpleMailMessage capturedMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private Trainer trainer() {
        return Trainer.builder()
                .name("Trainer One")
                .email("trainer@example.com")
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .build();
    }
}
