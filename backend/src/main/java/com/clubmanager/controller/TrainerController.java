package com.clubmanager.controller;

import com.clubmanager.domain.Team;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.TrainerTeamHistoryResponse;
import com.clubmanager.dto.TrainerCreateRequest;
import com.clubmanager.dto.TrainerResponse;
import com.clubmanager.dto.TrainerSummaryResponse;
import com.clubmanager.dto.TrainerUpdateRequest;
import com.clubmanager.mapper.TrainerMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.TrainerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainerMapper trainerMapper;
    private final AuditEventService auditEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerResponse createTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        var trainer = trainerService.createTrainer(request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.TRAINER,
                trainer.getUuid(),
                trainer.getName(),
                "Trainer created: " + trainer.getName());
        return trainerMapper.toResponse(trainer);
    }

    @GetMapping
    public PageResponse<TrainerSummaryResponse> getAllTrainers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return PageResponse.from(trainerService.searchTrainers(name, active, pageable)
                .map(trainerMapper::toSummaryResponse));
    }

    @GetMapping("/{uuid}")
    public TrainerResponse getTrainerByUuid(@PathVariable UUID uuid) {
        return trainerMapper.toResponse(trainerService.getTrainerByUuid(uuid));
    }

    @GetMapping("/{uuid}/teams")
    public java.util.List<TrainerTeamHistoryResponse> getTrainerTeams(@PathVariable UUID uuid) {
        var trainer = trainerService.getTrainerByUuid(uuid);
        return trainerService.getTeamHistory(uuid).stream()
                .map(team -> toTeamHistoryResponse(team, trainer.getUuid()))
                .toList();
    }

    @PutMapping("/{uuid}")
    public TrainerResponse updateTrainer(@PathVariable UUID uuid, @Valid @RequestBody TrainerUpdateRequest request) {
        var trainer = trainerService.updateTrainer(uuid, request);
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.TRAINER,
                trainer.getUuid(),
                trainer.getName(),
                "Trainer updated: " + trainer.getName());
        return trainerMapper.toResponse(trainer);
    }

    @PatchMapping("/{uuid}/deactivate")
    public TrainerResponse deactivateTrainer(@PathVariable UUID uuid) {
        var trainer = trainerService.deactivateTrainer(uuid);
        auditEventService.record(
                AuditEventService.DEACTIVATED,
                AuditEventService.TRAINER,
                trainer.getUuid(),
                trainer.getName(),
                "Trainer deactivated: " + trainer.getName());
        return trainerMapper.toResponse(trainer);
    }

    @PatchMapping("/{uuid}/reactivate")
    public TrainerResponse reactivateTrainer(@PathVariable UUID uuid) {
        var trainer = trainerService.reactivateTrainer(uuid);
        auditEventService.record(
                AuditEventService.REACTIVATED,
                AuditEventService.TRAINER,
                trainer.getUuid(),
                trainer.getName(),
                "Trainer reactivated: " + trainer.getName());
        return trainerMapper.toResponse(trainer);
    }

    private TrainerTeamHistoryResponse toTeamHistoryResponse(Team team, UUID trainerUuid) {
        String role = team.getTrainer().getUuid().equals(trainerUuid) ? "Trainer" : "Assistant trainer";
        return new TrainerTeamHistoryResponse(
                team.getUuid(),
                team.getAgeGroup(),
                team.getAgeCategory(),
                team.getTeamCategory(),
                role,
                team.isActive());
    }
}
