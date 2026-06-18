package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.clubmanager.service.AppMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
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
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void createEvaluation_WithValidToken_ReturnsCreatedEvaluation() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEvaluationJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.title").value("Spring Tryouts"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.ageGroup").value("Under 13"))
                .andExpect(jsonPath("$.teamCategory").value("MASCULINE"))
                .andExpect(jsonPath("$.createdDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.limitDate").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.expired").value(false))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void createEvaluation_WithBlankTitle_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEvaluationJson().replace("Spring Tryouts", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createEvaluation_WithPastLimitDate_ReturnsExpiredEvaluation() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Expired Tryouts",
                                  "ageGroup": "Under 13",
                                  "teamCategory": "MASCULINE",
                                  "limitDate": "%s"
                                }
                                """.formatted(LocalDate.now().minusDays(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.limitDate").value(LocalDate.now().minusDays(1).toString()))
                .andExpect(jsonPath("$.expired").value(true));
    }

    @Test
    void createEvaluation_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEvaluationJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllEvaluations_WithValidToken_ReturnsPaginatedList() throws Exception {
        String title = "List Evaluation " + System.nanoTime();
        String evaluationUuid = createEvaluation(title);

        mockMvc.perform(get("/api/v1/evaluations")
                        .param("title", title)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").isString())
                .andExpect(jsonPath("$.content[0].uuid").value(evaluationUuid))
                .andExpect(jsonPath("$.content[0].ageGroup").isString())
                .andExpect(jsonPath("$.content[0].expired").value(false))
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void getEvaluationByUuid_WithValidToken_ReturnsFullEvaluation() throws Exception {
        String evaluationUuid = createEvaluation();

        mockMvc.perform(get("/api/v1/evaluations/{uuid}", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(evaluationUuid))
                .andExpect(jsonPath("$.ageGroup").value("Under 13"))
                .andExpect(jsonPath("$.teamCategory").value("MASCULINE"))
                .andExpect(jsonPath("$.limitDate").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.expired").value(false))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void updateEvaluation_WithValidRequest_ReturnsUpdatedEvaluation() throws Exception {
        String evaluationUuid = createEvaluation();

        mockMvc.perform(put("/api/v1/evaluations/{uuid}", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Summer Tryouts",
                                  "ageGroup": "Under 15",
                                  "teamCategory": "FEMININE",
                                  "limitDate": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(45))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Summer Tryouts"))
                .andExpect(jsonPath("$.ageGroup").value("Under 15"))
                .andExpect(jsonPath("$.teamCategory").value("FEMININE"))
                .andExpect(jsonPath("$.limitDate").value(LocalDate.now().plusDays(45).toString()));
    }

    @Test
    void eventCannotCompleteUntilParticipationRecordedThenParticipantEvaluationIsRequiredBeforeFinalize() throws Exception {
        String evaluationUuid = createEvaluation();
        String playerUuid = createPlayer("EV-001");
        assignPlayer(evaluationUuid, playerUuid);
        String eventUuid = createEvent(evaluationUuid);
        double startBefore = count(AppMetricsService.EVALUATION_STARTED);
        double eventCompletedBefore = count(AppMetricsService.EVALUATION_EVENT_COMPLETED);
        double finalizedBefore = count(AppMetricsService.EVALUATION_FINALIZED);
        startEvaluation(evaluationUuid);
        assertThat(count(AppMetricsService.EVALUATION_STARTED)).isEqualTo(startBefore + 1.0);

        mockMvc.perform(patch("/api/v1/evaluation-events/{eventUuid}/complete", eventUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All assigned players must have participation before closing the event"));

        mockMvc.perform(put("/api/v1/evaluation-events/{eventUuid}/attendance/{playerUuid}", eventUuid, playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PRESENT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerUuid").value(playerUuid))
                .andExpect(jsonPath("$.status").value("PRESENT"))
                .andExpect(jsonPath("$.skillLevel").doesNotExist());

        mockMvc.perform(patch("/api/v1/evaluation-events/{eventUuid}/complete", eventUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        assertThat(count(AppMetricsService.EVALUATION_EVENT_COMPLETED)).isEqualTo(eventCompletedBefore + 1.0);

        mockMvc.perform(get("/api/v1/players/{uuid}", playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSkillLevel").doesNotExist());

        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/finalize", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All assigned players must be evaluated before finalizing"));

        mockMvc.perform(put("/api/v1/evaluations/{evaluationUuid}/results/{playerUuid}", evaluationUuid, playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "levelResult": "SKILLED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.evaluationUuid").value(evaluationUuid))
                .andExpect(jsonPath("$.playerUuid").value(playerUuid))
                .andExpect(jsonPath("$.playerName").value("Evaluation Player EV-001"))
                .andExpect(jsonPath("$.levelResult").value("SKILLED"))
                .andExpect(jsonPath("$.attendanceStatus").value("PRESENT"))
                .andExpect(jsonPath("$.id").doesNotExist());

        mockMvc.perform(get("/api/v1/players/{uuid}", playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSkillLevel").value("SKILLED"));

        mockMvc.perform(get("/api/v1/players/{uuid}/skill-history", playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillLevel").value("SKILLED"))
                .andExpect(jsonPath("$[0].changedByAdminName").value("Admin"))
                .andExpect(jsonPath("$[0].description").value("Evaluation finalized: Spring Tryouts"))
                .andExpect(jsonPath("$[0].id").doesNotExist());

        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/finalize", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"));
        assertThat(count(AppMetricsService.EVALUATION_FINALIZED)).isEqualTo(finalizedBefore + 1.0);

        mockMvc.perform(get("/api/v1/evaluations/{uuid}/results", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").isString())
                .andExpect(jsonPath("$[0].evaluationUuid").value(evaluationUuid))
                .andExpect(jsonPath("$[0].playerUuid").value(playerUuid))
                .andExpect(jsonPath("$[0].playerName").value("Evaluation Player EV-001"))
                .andExpect(jsonPath("$[0].levelResult").value("SKILLED"))
                .andExpect(jsonPath("$[0].attendanceStatus").value("PRESENT"))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void finalizedEvaluationCannotChangeEvents() throws Exception {
        String evaluationUuid = createEvaluation();
        String eventUuid = createEvent(evaluationUuid);

        mockMvc.perform(patch("/api/v1/evaluation-events/{eventUuid}/cancel", eventUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancelReason": "Closed before finalization"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/finalize", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"));

        mockMvc.perform(post("/api/v1/evaluations/{evaluationUuid}/events", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "place": "Second Field",
                                  "eventDate": "%s",
                                  "startTime": "19:00",
                                  "durationMinutes": 60
                                }
                                """.formatted(LocalDate.now().plusDays(8))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Finalized evaluations cannot be changed"));

        mockMvc.perform(patch("/api/v1/evaluation-events/{eventUuid}/cancel", eventUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancelReason": "Closed evaluation"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Finalized evaluations cannot be changed"));

        mockMvc.perform(patch("/api/v1/evaluation-events/{eventUuid}/complete", eventUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Finalized evaluations cannot be changed"));
    }

    @Test
    void removeEvaluationPlayer_WithValidAssignment_ReturnsInactiveAssignment() throws Exception {
        String evaluationUuid = createEvaluation();
        String playerUuid = createPlayer("EV-002");
        String assignmentUuid = assignPlayer(evaluationUuid, playerUuid);

        mockMvc.perform(delete("/api/v1/evaluations/{evaluationUuid}/players/{assignmentUuid}", evaluationUuid, assignmentUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void startEvaluation_WhenOpen_ReturnsInProgressEvaluation() throws Exception {
        String evaluationUuid = createEvaluation();
        createEvent(evaluationUuid);

        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/start", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void startEvaluation_WithoutEvents_ReturnsBadRequest() throws Exception {
        String evaluationUuid = createEvaluation();

        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/start", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one event is required before starting an evaluation"));
    }

    @Test
    void finalizeEvaluation_WhenAllEventsClosed_ReturnsFinalizedEvaluation() throws Exception {
        String evaluationUuid = createEvaluation();
        String eventUuid = createEvent(evaluationUuid);

        mockMvc.perform(patch("/api/v1/evaluation-events/{eventUuid}/cancel", eventUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancelReason": "No players assigned"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/finalize", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"));
    }

    @Test
    void finalizeEvaluation_WithScheduledEvent_ReturnsBadRequest() throws Exception {
        String evaluationUuid = createEvaluation();
        createEvent(evaluationUuid);

        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/finalize", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All evaluation events must be completed or canceled before finalizing"));
    }

    private String createEvaluation() throws Exception {
        return createEvaluation("Spring Tryouts");
    }

    private String createEvaluation(String title) throws Exception {
        String response = mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEvaluationJson(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String assignPlayer(String evaluationUuid, String playerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/evaluations/{evaluationUuid}/players", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerUuid": "%s"
                                }
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String createEvent(String evaluationUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/evaluations/{evaluationUuid}/events", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "place": "Main Field",
                                  "eventDate": "%s",
                                  "startTime": "18:00",
                                  "durationMinutes": 90
                                }
                                """.formatted(LocalDate.now().plusDays(7))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private void startEvaluation(String evaluationUuid) throws Exception {
        mockMvc.perform(patch("/api/v1/evaluations/{uuid}/start", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    private String createPlayer(String registrationNumber) throws Exception {
        String response = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Evaluation Player %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "2012-05-20",
                                  "teamCategory": "MASCULINE",
                                  "positions": ["MIDFIELD"],
                                  "registrationNumber": "%s",
                                  "memberSince": "%s"
                                }
                                """.formatted(registrationNumber, registrationNumber, LocalDate.now().minusYears(1))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String validEvaluationJson() {
        return validEvaluationJson("Spring Tryouts");
    }

    private String validEvaluationJson(String title) {
        return """
                {
                  "title": "%s",
                  "ageGroup": "Under 13",
                  "teamCategory": "MASCULINE",
                  "limitDate": "%s"
                }
                """.formatted(title, LocalDate.now().plusDays(30));
    }

    private double count(String name) {
        Counter counter = meterRegistry.find(name).counter();
        return counter == null ? 0.0 : counter.count();
    }
}
