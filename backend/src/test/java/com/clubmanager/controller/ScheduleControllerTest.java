package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.clubmanager.service.AppMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void getFields_WithValidToken_ReturnsSeededFields() throws Exception {
        mockMvc.perform(get("/api/v1/fields")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").isString())
                .andExpect(jsonPath("$[*].name", hasItem("Main Field")))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void createSchedule_WithValidToken_ReturnsCreatedSchedule() throws Exception {
        String teamUuid = createTeam();
        String fieldUuid = getFirstFieldUuid();
        String dateTime = LocalDateTime.now().plusDays(5).withNano(0).toString();
        double before = count(AppMetricsService.SCHEDULE_CREATED);

        mockMvc.perform(post("/api/v1/schedules")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validScheduleJson(teamUuid, fieldUuid, dateTime)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.teamUuid").value(teamUuid))
                .andExpect(jsonPath("$.fieldUuid").value(fieldUuid))
                .andExpect(jsonPath("$.dateTime").value(dateTime))
                .andExpect(jsonPath("$.durationMinutes").value(90))
                .andExpect(jsonPath("$.type").value("TRAINING"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.id").doesNotExist());

        assertThat(count(AppMetricsService.SCHEDULE_CREATED)).isEqualTo(before + 1.0);
    }

    @Test
    void createSchedule_WithPastDateTime_ReturnsBadRequest() throws Exception {
        String teamUuid = createTeam();
        String fieldUuid = getFirstFieldUuid();
        String dateTime = LocalDateTime.now().minusDays(1).withNano(0).toString();

        mockMvc.perform(post("/api/v1/schedules")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validScheduleJson(teamUuid, fieldUuid, dateTime)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Schedule date and time must be in the future"));
    }

    @Test
    void getAllSchedules_WithTeamFilter_ReturnsPaginatedList() throws Exception {
        String teamUuid = createTeam();
        String fieldUuid = getFirstFieldUuid();
        String scheduleUuid = createSchedule(teamUuid, fieldUuid);

        mockMvc.perform(get("/api/v1/schedules")
                        .param("teamUuid", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].uuid", hasItem(scheduleUuid)))
                .andExpect(jsonPath("$.content[0].teamUuid").isString())
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void cancelSchedule_WithValidToken_ReturnsCanceledSchedule() throws Exception {
        String teamUuid = createTeam();
        String fieldUuid = getFirstFieldUuid();
        String scheduleUuid = createSchedule(teamUuid, fieldUuid);
        double before = count(AppMetricsService.SCHEDULE_CANCELED);

        mockMvc.perform(patch("/api/v1/schedules/{uuid}/cancel", scheduleUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cancelReason": "Weather"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(scheduleUuid))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.cancelReason").value("Weather"));

        assertThat(count(AppMetricsService.SCHEDULE_CANCELED)).isEqualTo(before + 1.0);
    }

    @Test
    void createSchedule_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String createSchedule(String teamUuid, String fieldUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/schedules")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validScheduleJson(teamUuid, fieldUuid, LocalDateTime.now().plusDays(3).withNano(0).toString())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String getFirstFieldUuid() throws Exception {
        String response = mockMvc.perform(get("/api/v1/fields")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$[0].uuid");
    }

    private String createTeam() throws Exception {
        String trainerUuid = createTrainer();
        String response = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identification": "Schedule Team %s",
                                  "ageCategory": "U13",
                                  "teamCategory": "MASCULINE",
                                  "trainerUuid": "%s"
                                }
                                """.formatted(System.nanoTime(), trainerUuid)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String createTrainer() throws Exception {
        String response = mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Schedule Trainer %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "1988-04-20",
                                  "email": "schedule-trainer-%s@club.com",
                                  "phone": "555-0100",
                                  "memberSince": "%s"
                                }
                                """.formatted(System.nanoTime(), System.nanoTime(), LocalDate.now().minusYears(5))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String validScheduleJson(String teamUuid, String fieldUuid, String dateTime) {
        return """
                {
                  "teamUuid": "%s",
                  "fieldUuid": "%s",
                  "dateTime": "%s",
                  "durationMinutes": 90,
                  "type": "TRAINING",
                  "notes": "Evening practice"
                }
                """.formatted(teamUuid, fieldUuid, dateTime);
    }

    private double count(String name) {
        Counter counter = meterRegistry.find(name).counter();
        return counter == null ? 0.0 : counter.count();
    }
}
