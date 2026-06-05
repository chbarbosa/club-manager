package com.clubmanager.controller;

import com.clubmanager.dto.EvaluationPlayerAssignRequest;
import com.clubmanager.dto.EvaluationPlayerResponse;
import com.clubmanager.mapper.EvaluationPlayerMapper;
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

    public EvaluationPlayerController(
            EvaluationPlayerService evaluationPlayerService, EvaluationPlayerMapper evaluationPlayerMapper) {
        this.evaluationPlayerService = evaluationPlayerService;
        this.evaluationPlayerMapper = evaluationPlayerMapper;
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
        return evaluationPlayerMapper.toResponse(evaluationPlayerService.assignPlayer(evaluationUuid, request));
    }

    @DeleteMapping("/{assignmentUuid}")
    public EvaluationPlayerResponse removePlayer(@PathVariable UUID evaluationUuid, @PathVariable UUID assignmentUuid) {
        return evaluationPlayerMapper.toResponse(evaluationPlayerService.removePlayer(evaluationUuid, assignmentUuid));
    }
}
