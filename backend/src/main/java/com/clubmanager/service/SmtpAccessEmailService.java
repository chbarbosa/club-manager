package com.clubmanager.service;

import com.clubmanager.config.MailConfigProperties;
import com.clubmanager.domain.Trainer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
public class SmtpAccessEmailService implements AccessEmailService {

    private final JavaMailSender mailSender;
    private final MailConfigProperties mailConfigProperties;

    @Override
    public void sendTrainerAccessCode(Trainer trainer, String code) {
        send(
                trainer.getEmail(),
                "Club Manager trainer access",
                """
                Hello %s,

                Your trainer access is ready.

                Use this five-digit code to confirm your first password:
                %s

                Open: %s/trainer-password/confirm

                This code expires soon. If you did not expect this email, contact your club administrator.
                """.formatted(trainer.getName(), code, mailConfigProperties.appUrl()));
    }

    @Override
    public void sendTrainerPasswordResetCode(Trainer trainer, String code) {
        send(
                trainer.getEmail(),
                "Club Manager trainer password reset",
                """
                Hello %s,

                Use this five-digit code to reset your trainer password:
                %s

                Open: %s/account/password

                If you did not request this reset, contact your club administrator.
                """.formatted(trainer.getName(), code, mailConfigProperties.appUrl()));
    }

    @Override
    public void sendSupportAccessPassword(String email, String password) {
        send(
                email,
                "Club Manager support access",
                """
                Hello,

                Temporary read-only support access has been created for this club instance.

                Login email: %s
                Temporary password: %s
                Open: %s/login

                This access expires in 5 hours. All viewed data is recorded for the club administrators.
                """.formatted(email, password, mailConfigProperties.appUrl()));
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailConfigProperties.from());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
