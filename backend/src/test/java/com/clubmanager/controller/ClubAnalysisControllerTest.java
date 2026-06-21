package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clubmanager.repository.ClubAnalysisRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
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
class ClubAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClubAnalysisRepository clubAnalysisRepository;

    @BeforeEach
    void setUp() {
        clubAnalysisRepository.deleteAll();
    }

    @Test
    void currentAnalysis_WithAdminToken_GeneratesOnceAndReturnsSnapshot() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);
        String playerUuid = createPlayer();
        assignPlayer(teamUuid, playerUuid);

        String firstResponse = mockMvc.perform(get("/api/v1/club-analysis/current")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.analysisDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.totalItems").isNumber())
                .andExpect(jsonPath("$.warningCount").isNumber())
                .andExpect(jsonPath("$.criticalCount").isNumber())
                .andExpect(jsonPath("$.items[*].code", hasItem("PLAYER_TOTAL")))
                .andExpect(jsonPath("$.items[*].code", hasItem("TEAM_MISSING_ASSISTANTS")))
                .andExpect(jsonPath("$.items[*].id").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String analysisUuid = JsonPath.read(firstResponse, "$.uuid");

        mockMvc.perform(get("/api/v1/club-analysis/current")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(analysisUuid));

        mockMvc.perform(get("/api/v1/club-analysis")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].uuid", hasItem(analysisUuid)));

        mockMvc.perform(get("/api/v1/club-analysis/{uuid}", analysisUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(analysisUuid))
                .andExpect(jsonPath("$.items[?(@.code == 'TEAM_MISSING_ASSISTANTS')].affectedRecords[0].entityType", hasItem("TEAM")))
                .andExpect(jsonPath("$.items[?(@.code == 'TEAM_MISSING_ASSISTANTS')].affectedRecords[*].uuid", hasItem(teamUuid)));
    }

    @Test
    void currentAnalysis_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/club-analysis/current"))
                .andExpect(status().isForbidden());
    }

    private String createTrainer() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String response = mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Analysis Trainer %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "1988-04-20",
                                  "email": "analysis-trainer-%s@club.com",
                                  "phone": "555-0100",
                                  "memberSince": "%s"
                                }
                                """.formatted(suffix, suffix, LocalDate.now().minusYears(5))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String createTeam(String trainerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identification": "Analysis Team %s",
                                  "ageCategory": "U19_PLUS",
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

    private String createPlayer() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String response = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Analysis Player %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "2005-03-15",
                                  "teamCategory": "MASCULINE",
                                  "positions": ["MIDFIELD"],
                                  "registrationNumber": "ANALYSIS-%s",
                                  "memberSince": "2020-01-01"
                                }
                                """.formatted(suffix, suffix)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private void assignPlayer(String teamUuid, String playerUuid) throws Exception {
        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s", "jerseyNumber": 10}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated());
    }
}
