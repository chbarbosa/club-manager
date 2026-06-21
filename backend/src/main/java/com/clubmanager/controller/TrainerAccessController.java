package com.clubmanager.controller;

import com.clubmanager.dto.TrainerAccessInviteRequest;
import com.clubmanager.dto.TrainerPasswordConfirmRequest;
import com.clubmanager.dto.TrainerPasswordResetConfirmRequest;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.TrainerAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainer-access")
@RequiredArgsConstructor
public class TrainerAccessController {

    private final TrainerAccessService trainerAccessService;
    private final AuditEventService auditEventService;

    @PostMapping("/invitations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ADMIN')")
    public void inviteTrainer(@Valid @RequestBody TrainerAccessInviteRequest request) {
        var trainer = trainerAccessService.inviteTrainer(request.trainerUuid());
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.TRAINER,
                trainer.getUuid(),
                trainer.getName(),
                "Trainer access invitation sent: " + trainer.getName());
    }

    @PostMapping("/confirm-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmFirstPassword(@Valid @RequestBody TrainerPasswordConfirmRequest request) {
        trainerAccessService.confirmFirstPassword(request);
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('TRAINER')")
    public void requestPasswordReset(Authentication authentication) {
        trainerAccessService.requestPasswordReset(authentication.getName());
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TRAINER')")
    public void confirmPasswordReset(
            Authentication authentication,
            @Valid @RequestBody TrainerPasswordResetConfirmRequest request) {
        trainerAccessService.confirmPasswordReset(authentication.getName(), request);
    }
}
