package com.clubmanager.controller;

import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.EvaluationCreateRequest;
import com.clubmanager.dto.EvaluationResponse;
import com.clubmanager.dto.EvaluationResultResponse;
import com.clubmanager.dto.EvaluationResultUpdateRequest;
import com.clubmanager.dto.EvaluationSummaryResponse;
import com.clubmanager.dto.EvaluationUpdateRequest;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.mapper.EvaluationMapper;
import com.clubmanager.mapper.EvaluationResultMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.EvaluationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/v1/evaluations")
@PreAuthorize("hasRole('ADMIN')")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final EvaluationMapper evaluationMapper;
    private final EvaluationResultMapper evaluationResultMapper;
    private final AuditEventService auditEventService;

    public EvaluationController(
            EvaluationService evaluationService,
            EvaluationMapper evaluationMapper,
            EvaluationResultMapper evaluationResultMapper,
            AuditEventService auditEventService) {
        this.evaluationService = evaluationService;
        this.evaluationMapper = evaluationMapper;
        this.evaluationResultMapper = evaluationResultMapper;
        this.auditEventService = auditEventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationResponse createEvaluation(@Valid @RequestBody EvaluationCreateRequest request) {
        var evaluation = evaluationService.createEvaluation(request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.EVALUATION,
                evaluation.getUuid(),
                evaluation.getTitle(),
                "Evaluation created: " + evaluation.getTitle());
        return evaluationMapper.toResponse(evaluation);
    }

    @GetMapping
    public PageResponse<EvaluationSummaryResponse> getAllEvaluations(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) TeamCategory teamCategory,
            @RequestParam(required = false) EvaluationStatus status,
            Pageable pageable) {
        return PageResponse.from(evaluationService.searchEvaluations(title, ageGroup, teamCategory, status, pageable)
                .map(evaluationMapper::toSummaryResponse));
    }

    @GetMapping("/{uuid}")
    public EvaluationResponse getEvaluationByUuid(@PathVariable UUID uuid) {
        return evaluationMapper.toResponse(evaluationService.getEvaluationByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public EvaluationResponse updateEvaluation(@PathVariable UUID uuid, @Valid @RequestBody EvaluationUpdateRequest request) {
        var evaluation = evaluationService.updateEvaluation(uuid, request);
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.EVALUATION,
                evaluation.getUuid(),
                evaluation.getTitle(),
                "Evaluation updated: " + evaluation.getTitle());
        return evaluationMapper.toResponse(evaluation);
    }

    @PatchMapping("/{uuid}/start")
    public EvaluationResponse startEvaluation(@PathVariable UUID uuid) {
        var evaluation = evaluationService.startEvaluation(uuid);
        auditEventService.record(
                AuditEventService.STARTED,
                AuditEventService.EVALUATION,
                evaluation.getUuid(),
                evaluation.getTitle(),
                "Evaluation started: " + evaluation.getTitle());
        return evaluationMapper.toResponse(evaluation);
    }

    @PatchMapping("/{uuid}/finalize")
    public EvaluationResponse finalizeEvaluation(@PathVariable UUID uuid) {
        var evaluation = evaluationService.finalizeEvaluation(uuid);
        auditEventService.record(
                AuditEventService.FINALIZED,
                AuditEventService.EVALUATION,
                evaluation.getUuid(),
                evaluation.getTitle(),
                "Evaluation finalized: " + evaluation.getTitle());
        return evaluationMapper.toResponse(evaluation);
    }

    @GetMapping("/{uuid}/results")
    public List<EvaluationResultResponse> getResults(@PathVariable UUID uuid) {
        return evaluationService.getResults(uuid).stream()
                .map(evaluationResultMapper::toResponse)
                .toList();
    }

    @PutMapping("/{uuid}/results/{playerUuid}")
    public EvaluationResultResponse updateResult(
            @PathVariable UUID uuid,
            @PathVariable UUID playerUuid,
            @Valid @RequestBody EvaluationResultUpdateRequest request) {
        var result = evaluationService.updateResult(uuid, playerUuid, request);
        auditEventService.record(
                AuditEventService.EVALUATED,
                AuditEventService.EVALUATION_RESULT,
                result.getUuid(),
                result.getPlayer().getName(),
                "Evaluation result saved for player: " + result.getPlayer().getName());
        return evaluationResultMapper.toResponse(result);
    }
}
