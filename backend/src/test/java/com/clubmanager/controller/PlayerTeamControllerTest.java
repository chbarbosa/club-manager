package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
class PlayerTeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void assignPlayer_WithValidToken_ReturnsCreatedAssignment() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid, "MASCULINE");
        String playerUuid = createPlayer("REG-PT-100", "MASCULINE");

        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s"}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.playerUuid").value(playerUuid))
                .andExpect(jsonPath("$.playerName").value("Joao Silva"))
                .andExpect(jsonPath("$.teamUuid").value(teamUuid))
                .andExpect(jsonPath("$.assignedDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void assignPlayer_WithMismatchedTeamCategory_ReturnsBadRequest() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid, "FEMININE");
        String playerUuid = createPlayer("REG-PT-101", "MASCULINE");

        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s"}
                                """.formatted(playerUuid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void assignPlayer_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", "00000000-0000-0000-0000-000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "00000000-0000-0000-0000-000000000000"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void getActiveRoster_WithValidToken_ReturnsCurrentAssignments() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid, "MASCULINE");
        String playerUuid = createPlayer("REG-PT-102", "MASCULINE");
        String assignmentUuid = assignPlayer(teamUuid, playerUuid);

        mockMvc.perform(get("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").isString())
                .andExpect(jsonPath("$[*].uuid", hasItem(assignmentUuid)))
                .andExpect(jsonPath("$[0].playerUuid").isString())
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void removePlayer_WithValidToken_ReturnsInactiveAssignment() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid, "MASCULINE");
        String playerUuid = createPlayer("REG-PT-103", "MASCULINE");
        String assignmentUuid = assignPlayer(teamUuid, playerUuid);

        mockMvc.perform(delete("/api/v1/teams/{teamUuid}/players/{assignmentUuid}", teamUuid, assignmentUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(assignmentUuid))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.removedDate").value(LocalDate.now().toString()));

        mockMvc.perform(get("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private String assignPlayer(String teamUuid, String playerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s"}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String createPlayer(String registrationNumber, String teamCategory) throws Exception {
        String response = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joao Silva",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "2012-03-15",
                                  "teamCategory": "%s",
                                  "registrationNumber": "%s",
                                  "memberSince": "2020-01-01"
                                }
                                """.formatted(teamCategory, registrationNumber)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String createTeam(String trainerUuid, String teamCategory) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ageGroup": "Under 13",
                                  "teamCategory": "%s",
                                  "trainerUuid": "%s"
                                }
                                """.formatted(teamCategory, trainerUuid)))
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
                                  "name": "Carlos Mendes",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "1988-04-20",
                                  "email": "carlos@club.com",
                                  "phone": "555-0100",
                                  "memberSince": "%s"
                                }
                                """.formatted(LocalDate.now().minusYears(5))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }
}

