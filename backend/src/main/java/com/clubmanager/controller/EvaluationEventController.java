package com.clubmanager.controller;

import com.clubmanager.dto.EvaluationEventAttendanceResponse;
import com.clubmanager.dto.EvaluationEventAttendanceUpdateRequest;
import com.clubmanager.dto.EvaluationEventCancelRequest;
import com.clubmanager.dto.EvaluationEventCreateRequest;
import com.clubmanager.dto.EvaluationEventResponse;
import com.clubmanager.mapper.EvaluationEventAttendanceMapper;
import com.clubmanager.mapper.EvaluationEventMapper;
import com.clubmanager.service.EvaluationEventService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class EvaluationEventController {

    private final EvaluationEventService evaluationEventService;
    private final EvaluationEventMapper evaluationEventMapper;
    private final EvaluationEventAttendanceMapper attendanceMapper;

    public EvaluationEventController(
            EvaluationEventService evaluationEventService,
            EvaluationEventMapper evaluationEventMapper,
            EvaluationEventAttendanceMapper attendanceMapper) {
        this.evaluationEventService = evaluationEventService;
        this.evaluationEventMapper = evaluationEventMapper;
        this.attendanceMapper = attendanceMapper;
    }

    @GetMapping("/api/v1/evaluations/{evaluationUuid}/events")
    public List<EvaluationEventResponse> getEvents(@PathVariable UUID evaluationUuid) {
        return evaluationEventService.getEvents(evaluationUuid).stream()
                .map(evaluationEventMapper::toResponse)
                .toList();
    }

    @PostMapping("/api/v1/evaluations/{evaluationUuid}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationEventResponse createEvent(
            @PathVariable UUID evaluationUuid,
            @Valid @RequestBody EvaluationEventCreateRequest request) {
        return evaluationEventMapper.toResponse(evaluationEventService.createEvent(evaluationUuid, request));
    }

    @GetMapping("/api/v1/evaluation-events/{eventUuid}/attendance")
    public List<EvaluationEventAttendanceResponse> getAttendance(@PathVariable UUID eventUuid) {
        return evaluationEventService.getAttendance(eventUuid).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    @PutMapping("/api/v1/evaluation-events/{eventUuid}/attendance/{playerUuid}")
    public EvaluationEventAttendanceResponse updateAttendance(
            @PathVariable UUID eventUuid,
            @PathVariable UUID playerUuid,
            @Valid @RequestBody EvaluationEventAttendanceUpdateRequest request) {
        return attendanceMapper.toResponse(evaluationEventService.updateAttendance(eventUuid, playerUuid, request));
    }

    @PatchMapping("/api/v1/evaluation-events/{eventUuid}/complete")
    public EvaluationEventResponse completeEvent(@PathVariable UUID eventUuid) {
        return evaluationEventMapper.toResponse(evaluationEventService.completeEvent(eventUuid));
    }

    @PatchMapping("/api/v1/evaluation-events/{eventUuid}/cancel")
    public EvaluationEventResponse cancelEvent(
            @PathVariable UUID eventUuid,
            @RequestBody(required = false) EvaluationEventCancelRequest request) {
        return evaluationEventMapper.toResponse(evaluationEventService.cancelEvent(eventUuid, request));
    }
}
