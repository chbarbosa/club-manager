package com.clubmanager.controller;

import com.clubmanager.domain.ScheduleStatus;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.ScheduleCancelRequest;
import com.clubmanager.dto.ScheduleCreateRequest;
import com.clubmanager.dto.ScheduleResponse;
import com.clubmanager.dto.ScheduleUpdateRequest;
import com.clubmanager.mapper.ScheduleMapper;
import com.clubmanager.service.AuditEventService;
import com.clubmanager.service.ScheduleService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/schedules")
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;
    private final AuditEventService auditEventService;

    public ScheduleController(
            ScheduleService scheduleService,
            ScheduleMapper scheduleMapper,
            AuditEventService auditEventService) {
        this.scheduleService = scheduleService;
        this.scheduleMapper = scheduleMapper;
        this.auditEventService = auditEventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createSchedule(@Valid @RequestBody ScheduleCreateRequest request) {
        var schedule = scheduleService.createSchedule(request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.SCHEDULE,
                schedule.getUuid(),
                scheduleLabel(schedule),
                "Schedule created: " + scheduleLabel(schedule));
        return scheduleMapper.toResponse(schedule);
    }

    @GetMapping
    public PageResponse<ScheduleResponse> getAllSchedules(
            @RequestParam(required = false) UUID teamUuid,
            @RequestParam(required = false) ScheduleStatus status,
            Pageable pageable) {
        return PageResponse.from(scheduleService.searchSchedules(teamUuid, status, pageable)
                .map(scheduleMapper::toResponse));
    }

    @GetMapping("/{uuid}")
    public ScheduleResponse getScheduleByUuid(@PathVariable UUID uuid) {
        return scheduleMapper.toResponse(scheduleService.getScheduleByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public ScheduleResponse updateSchedule(@PathVariable UUID uuid, @Valid @RequestBody ScheduleUpdateRequest request) {
        var schedule = scheduleService.updateSchedule(uuid, request);
        auditEventService.record(
                AuditEventService.UPDATED,
                AuditEventService.SCHEDULE,
                schedule.getUuid(),
                scheduleLabel(schedule),
                "Schedule updated: " + scheduleLabel(schedule));
        return scheduleMapper.toResponse(schedule);
    }

    @PatchMapping("/{uuid}/cancel")
    public ScheduleResponse cancelSchedule(
            @PathVariable UUID uuid,
            @RequestBody(required = false) ScheduleCancelRequest request) {
        var schedule = scheduleService.cancelSchedule(
                uuid,
                request == null ? new ScheduleCancelRequest(null) : request);
        auditEventService.record(
                AuditEventService.CANCELED,
                AuditEventService.SCHEDULE,
                schedule.getUuid(),
                scheduleLabel(schedule),
                "Schedule canceled: " + scheduleLabel(schedule));
        return scheduleMapper.toResponse(schedule);
    }

    private String scheduleLabel(com.clubmanager.domain.Schedule schedule) {
        return schedule.getTeam().getAgeGroup() + " " + schedule.getDateTime();
    }
}
