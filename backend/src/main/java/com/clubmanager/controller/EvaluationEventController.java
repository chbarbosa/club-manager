package com.clubmanager.controller;

import com.clubmanager.dto.EvaluationEventAttendanceResponse;
import com.clubmanager.dto.EvaluationEventAttendanceUpdateRequest;
import com.clubmanager.dto.EvaluationEventCancelRequest;
import com.clubmanager.dto.EvaluationEventCreateRequest;
import com.clubmanager.dto.EvaluationEventResponse;
import com.clubmanager.mapper.EvaluationEventAttendanceMapper;
import com.clubmanager.mapper.EvaluationEventMapper;
import com.clubmanager.service.AppMetricsService;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.EvaluationEventService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'TRAINER')")
@RequiredArgsConstructor
public class EvaluationEventController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationEventController.class);

    private final EvaluationEventService evaluationEventService;
    private final EvaluationEventMapper evaluationEventMapper;
    private final EvaluationEventAttendanceMapper attendanceMapper;
    private final AuditEventService auditEventService;
    private final AppMetricsService appMetricsService;



    @GetMapping("/api/v1/evaluations/{evaluationUuid}/events")
    public List<EvaluationEventResponse> getEvents(@PathVariable UUID evaluationUuid) {
        return evaluationEventService.getEvents(evaluationUuid).stream()
                .map(evaluationEventMapper::toResponse)
                .toList();
    }

    @PostMapping("/api/v1/evaluations/{evaluationUuid}/events")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EvaluationEventResponse createEvent(
            @PathVariable UUID evaluationUuid,
            @Valid @RequestBody EvaluationEventCreateRequest request) {
        var event = evaluationEventService.createEvent(evaluationUuid, request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.EVALUATION_EVENT,
                event.getUuid(),
                eventLabel(event),
                "Evaluation event created: " + eventLabel(event));
        return evaluationEventMapper.toResponse(event);
    }

    @GetMapping("/api/v1/evaluation-events/{eventUuid}/attendance")
    public List<EvaluationEventAttendanceResponse> getAttendance(@PathVariable UUID eventUuid) {
        return evaluationEventService.getAttendance(eventUuid).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    @PutMapping("/api/v1/evaluation-events/{eventUuid}/attendance/{playerUuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public EvaluationEventAttendanceResponse updateAttendance(
            @PathVariable UUID eventUuid,
            @PathVariable UUID playerUuid,
            @Valid @RequestBody EvaluationEventAttendanceUpdateRequest request) {
        var attendance = evaluationEventService.updateAttendance(eventUuid, playerUuid, request);
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.EVALUATION_ATTENDANCE,
                attendance.getUuid(),
                attendance.getPlayer().getName(),
                "Evaluation attendance updated for player: " + attendance.getPlayer().getName());
        return attendanceMapper.toResponse(attendance);
    }

    @PatchMapping("/api/v1/evaluation-events/{eventUuid}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public EvaluationEventResponse completeEvent(@PathVariable UUID eventUuid) {
        var event = evaluationEventService.completeEvent(eventUuid);
        auditEventService.record(
                AuditEventService.COMPLETED,
                AuditEventService.EVALUATION_EVENT,
                event.getUuid(),
                eventLabel(event),
                "Evaluation event completed: " + eventLabel(event));
        appMetricsService.recordEvaluationEventCompleted();
        LOGGER.info("Evaluation event completed uuid={}", event.getUuid());
        return evaluationEventMapper.toResponse(event);
    }

    @PatchMapping("/api/v1/evaluation-events/{eventUuid}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public EvaluationEventResponse cancelEvent(
            @PathVariable UUID eventUuid,
            @RequestBody(required = false) EvaluationEventCancelRequest request) {
        var event = evaluationEventService.cancelEvent(eventUuid, request);
        auditEventService.record(
                AuditEventService.CANCELED,
                AuditEventService.EVALUATION_EVENT,
                event.getUuid(),
                eventLabel(event),
                "Evaluation event canceled: " + eventLabel(event));
        return evaluationEventMapper.toResponse(event);
    }

    private String eventLabel(com.clubmanager.domain.EvaluationEvent event) {
        return event.getEvaluation().getTitle() + " " + event.getEventDate() + " " + event.getStartTime();
    }
}
