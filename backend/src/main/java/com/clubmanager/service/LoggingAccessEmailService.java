package com.clubmanager.service;

import com.clubmanager.domain.Trainer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingAccessEmailService implements AccessEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAccessEmailService.class);
    private static final Profiles DEV_PROFILE = Profiles.of("dev");

    private final Environment environment;

    @Override
    public void sendTrainerAccessCode(Trainer trainer, String code) {
        if (isDevProfile()) {
            LOGGER.info("Trainer access email prepared email={} devAccessCode={}", trainer.getEmail(), code);
            return;
        }
        LOGGER.info("Trainer access email prepared email={}", trainer.getEmail());
    }

    @Override
    public void sendTrainerPasswordResetCode(Trainer trainer, String code) {
        if (isDevProfile()) {
            LOGGER.info("Trainer password reset email prepared email={} devResetCode={}", trainer.getEmail(), code);
            return;
        }
        LOGGER.info("Trainer password reset email prepared email={}", trainer.getEmail());
    }

    @Override
    public void sendSupportAccessPassword(String email, String password) {
        LOGGER.info("Support access email prepared email={}", email);
    }

    private boolean isDevProfile() {
        return environment.acceptsProfiles(DEV_PROFILE);
    }
}
