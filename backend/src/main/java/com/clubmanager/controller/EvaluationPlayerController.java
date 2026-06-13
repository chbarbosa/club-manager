package com.clubmanager.controller;

import com.clubmanager.dto.EvaluationPlayerAssignRequest;
import com.clubmanager.dto.EvaluationPlayerResponse;
import com.clubmanager.mapper.EvaluationPlayerMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.EvaluationPlayerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/evaluations/{evaluationUuid}/players")
public class EvaluationPlayerController {

    private final EvaluationPlayerService evaluationPlayerService;
    private final EvaluationPlayerMapper evaluationPlayerMapper;
    private final AuditEventService auditEventService;

    public EvaluationPlayerController(
            EvaluationPlayerService evaluationPlayerService,
            EvaluationPlayerMapper evaluationPlayerMapper,
            AuditEventService auditEventService) {
        this.evaluationPlayerService = evaluationPlayerService;
        this.evaluationPlayerMapper = evaluationPlayerMapper;
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public List<EvaluationPlayerResponse> getActivePlayers(@PathVariable UUID evaluationUuid) {
        return evaluationPlayerService.getActivePlayers(evaluationUuid).stream()
                .map(evaluationPlayerMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationPlayerResponse assignPlayer(
            @PathVariable UUID evaluationUuid,
            @Valid @RequestBody EvaluationPlayerAssignRequest request) {
        var assignment = evaluationPlayerService.assignPlayer(evaluationUuid, request);
        auditEventService.record(
                AuditEventService.ASSIGNED,
                AuditEventService.EVALUATION_PLAYER,
                assignment.getUuid(),
                assignmentLabel(assignment),
                "Player assigned to evaluation: " + assignmentLabel(assignment));
        return evaluationPlayerMapper.toResponse(assignment);
    }

    @DeleteMapping("/{assignmentUuid}")
    public EvaluationPlayerResponse removePlayer(@PathVariable UUID evaluationUuid, @PathVariable UUID assignmentUuid) {
        var assignment = evaluationPlayerService.removePlayer(evaluationUuid, assignmentUuid);
        auditEventService.record(
                AuditEventService.REMOVED,
                AuditEventService.EVALUATION_PLAYER,
                assignment.getUuid(),
                assignmentLabel(assignment),
                "Player removed from evaluation: " + assignmentLabel(assignment));
        return evaluationPlayerMapper.toResponse(assignment);
    }

    private String assignmentLabel(com.clubmanager.domain.EvaluationPlayer assignment) {
        return assignment.getPlayer().getName() + " / " + assignment.getEvaluation().getTitle();
    }
}
