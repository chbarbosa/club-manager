package com.clubmanager.service;

import com.clubmanager.domain.Trainer;

public interface AccessEmailService {

    void sendTrainerAccessCode(Trainer trainer, String code);

    void sendTrainerPasswordResetCode(Trainer trainer, String code);

    void sendSupportAccessPassword(String email, String password);
}
