package com.clubmanager.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AppMetricsService {

    public static final String LOGIN_SUCCESS = "club.auth.login.success";
    public static final String LOGIN_FAILURE = "club.auth.login.failure";
    public static final String VALIDATION_FAILURE = "club.validation.failure";
    public static final String ACCESS_DENIED = "club.access.denied";
    public static final String SCHEDULE_CREATED = "club.schedule.created";
    public static final String SCHEDULE_CANCELED = "club.schedule.canceled";
    public static final String EVALUATION_STARTED = "club.evaluation.started";
    public static final String EVALUATION_FINALIZED = "club.evaluation.finalized";
    public static final String EVALUATION_EVENT_COMPLETED = "club.evaluation.event.completed";
    public static final String AUDIT_EVENT_RECORDED = "club.audit.event.recorded";
    public static final String MATCH_ANALYSIS_SAVED = "club.match.analysis.saved";

    private final MeterRegistry meterRegistry;

    public AppMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordLoginSuccess() {
        increment(LOGIN_SUCCESS);
    }

    public void recordLoginFailure() {
        increment(LOGIN_FAILURE);
    }

    public void recordValidationFailure(String type) {
        meterRegistry.counter(VALIDATION_FAILURE, "type", cleanTag(type)).increment();
    }

    public void recordAccessDenied() {
        increment(ACCESS_DENIED);
    }

    public void recordScheduleCreated() {
        increment(SCHEDULE_CREATED);
    }

    public void recordScheduleCanceled() {
        increment(SCHEDULE_CANCELED);
    }

    public void recordEvaluationStarted() {
        increment(EVALUATION_STARTED);
    }

    public void recordEvaluationFinalized() {
        increment(EVALUATION_FINALIZED);
    }

    public void recordEvaluationEventCompleted() {
        increment(EVALUATION_EVENT_COMPLETED);
    }

    public void recordAuditEventRecorded() {
        increment(AUDIT_EVENT_RECORDED);
    }

    public void recordMatchAnalysisSaved() {
        increment(MATCH_ANALYSIS_SAVED);
    }

    private void increment(String name) {
        meterRegistry.counter(name).increment();
    }

    private String cleanTag(String value) {
        return StringUtils.hasText(value) ? value.trim() : "unknown";
    }
}
