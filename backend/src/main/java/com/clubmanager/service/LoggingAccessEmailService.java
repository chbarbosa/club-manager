package com.clubmanager.service;

import com.clubmanager.domain.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingAccessEmailService implements AccessEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAccessEmailService.class);

    @Override
    public void sendTrainerAccessCode(Trainer trainer, String code) {
        LOGGER.info("Trainer access email prepared email={}", trainer.getEmail());
    }

    @Override
    public void sendTrainerPasswordResetCode(Trainer trainer, String code) {
        LOGGER.info("Trainer password reset email prepared email={}", trainer.getEmail());
    }

    @Override
    public void sendSupportAccessPassword(String email, String password) {
        LOGGER.info("Support access email prepared email={}", email);
    }
}
