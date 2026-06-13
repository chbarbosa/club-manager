package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clubmanager.service.AppMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ObservabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void actuatorPrometheus_WithAdminToken_ReturnsPrometheusText() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("# HELP")));
    }

    @Test
    void healthGroups_ReturnOkWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void loginMetrics_IncrementForSuccessAndFailure() throws Exception {
        double successBefore = count(AppMetricsService.LOGIN_SUCCESS);
        double failureBefore = count(AppMetricsService.LOGIN_FAILURE);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "admin123"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized());

        assertThat(count(AppMetricsService.LOGIN_SUCCESS)).isEqualTo(successBefore + 1.0);
        assertThat(count(AppMetricsService.LOGIN_FAILURE)).isEqualTo(failureBefore + 1.0);
    }

    @Test
    void validationAndAccessDeniedMetrics_IncrementForRejectedRequests() throws Exception {
        double validationBefore = countAll(AppMetricsService.VALIDATION_FAILURE);
        double accessDeniedBefore = count(AppMetricsService.ACCESS_DENIED);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "", "password": ""}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "No Token Admin",
                                  "email": "no-token@club.com",
                                  "username": "notoken",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isForbidden());

        assertThat(countAll(AppMetricsService.VALIDATION_FAILURE)).isEqualTo(validationBefore + 1.0);
        assertThat(count(AppMetricsService.ACCESS_DENIED)).isEqualTo(accessDeniedBefore + 1.0);
    }

    @Test
    void successfulMutation_IncrementsAuditEventMetric() throws Exception {
        double before = count(AppMetricsService.AUDIT_EVENT_RECORDED);

        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Observed Player %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "2013-03-15",
                                  "teamCategory": "MASCULINE",
                                  "positions": ["MIDFIELD"],
                                  "registrationNumber": "OBS-%s",
                                  "memberSince": "2020-01-01"
                                }
                                """.formatted(System.nanoTime(), System.nanoTime())))
                .andExpect(status().isCreated());

        assertThat(count(AppMetricsService.AUDIT_EVENT_RECORDED)).isEqualTo(before + 1.0);
    }

    private double count(String name) {
        Counter counter = meterRegistry.find(name).counter();
        return counter == null ? 0.0 : counter.count();
    }

    private double countAll(String name) {
        return meterRegistry.find(name).counters().stream()
                .mapToDouble(Counter::count)
                .sum();
    }
}
