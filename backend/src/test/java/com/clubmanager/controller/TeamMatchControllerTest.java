package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class TeamMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void matchFlow_WithAdminToken_CreatesListsUpdatesAndSavesPlayerAnalysis() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);
        String playerUuid = createPlayer();
        assignPlayer(teamUuid, playerUuid);
        createChampionship(teamUuid);

        String matchUuid = createMatch(teamUuid);

        mockMvc.perform(get("/api/v1/teams/{teamUuid}/matches", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].uuid", hasItem(matchUuid)))
                .andExpect(jsonPath("$[0].id").doesNotExist());

        mockMvc.perform(get("/api/v1/teams/{teamUuid}/matches/{matchUuid}", teamUuid, matchUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(matchUuid))
                .andExpect(jsonPath("$.playerAnalyses[0].playerUuid").value(playerUuid))
                .andExpect(jsonPath("$.playerAnalyses[0].playerAge").isNumber())
                .andExpect(jsonPath("$.playerAnalyses[0].playerCurrentSkillLevel").value(nullValue()))
                .andExpect(jsonPath("$.playerAnalyses[0].playerChampionshipCount").value(1))
                .andExpect(jsonPath("$.playerAnalyses[0].improvementTags").isArray())
                .andExpect(jsonPath("$.playerAnalyses[0].id").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());

        mockMvc.perform(put("/api/v1/teams/{teamUuid}/matches/{matchUuid}", teamUuid, matchUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opponent": "Updated Rivals",
                                  "place": "Secondary Field",
                                  "matchDateTime": "2026-08-12T18:30:00",
                                  "teamScore": 3,
                                  "opponentScore": 2,
                                  "notes": "Improved second half"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponent").value("Updated Rivals"))
                .andExpect(jsonPath("$.teamScore").value(3));

        double matchAnalysisBefore = count(AppMetricsService.MATCH_ANALYSIS_SAVED);
        mockMvc.perform(put("/api/v1/teams/{teamUuid}/matches/{matchUuid}/players/{playerUuid}", teamUuid, matchUuid, playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "improvementTags": ["Improve pass"],
                                  "highlightTags": ["Good passes"],
                                  "notes": "Good match reading"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerUuid").value(playerUuid))
                .andExpect(jsonPath("$.playerAge").isNumber())
                .andExpect(jsonPath("$.playerChampionshipCount").value(1))
                .andExpect(jsonPath("$.improvementTags[0]").value("Improve pass"))
                .andExpect(jsonPath("$.highlightTags[0]").value("Good passes"))
                .andExpect(jsonPath("$.notes").value("Good match reading"))
                .andExpect(jsonPath("$.id").doesNotExist());
        assertThat(count(AppMetricsService.MATCH_ANALYSIS_SAVED)).isEqualTo(matchAnalysisBefore + 1.0);
    }

    @Test
    void createMatch_WithNegativeScore_ReturnsBadRequest() throws Exception {
        String teamUuid = createTeam(createTrainer());

        mockMvc.perform(post("/api/v1/teams/{teamUuid}/matches", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opponent": "Rivals FC",
                                  "place": "Main Field",
                                  "matchDateTime": "2026-08-12T18:00:00",
                                  "teamScore": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private String createMatch(String teamUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams/{teamUuid}/matches", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opponent": "Rivals FC",
                                  "place": "Main Field",
                                  "matchDateTime": "2026-08-12T18:00:00",
                                  "teamScore": 2,
                                  "opponentScore": 1,
                                  "notes": "Friendly match"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String createChampionship(String teamUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/championships")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Match Cup %s",
                                  "description": "Match analysis championship context",
                                  "teamUuid": "%s",
                                  "startMonth": 8,
                                  "startYear": 2026,
                                  "endMonth": 10,
                                  "endYear": 2026,
                                  "expectedMatches": 8
                                }
                                """.formatted(System.nanoTime(), teamUuid)))
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
                                {"playerUuid": "%s"}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated());
    }

    private String createPlayer() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String response = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Match Player %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "2013-03-15",
                                  "teamCategory": "MASCULINE",
                                  "positions": ["MIDFIELD"],
                                  "registrationNumber": "MATCH-%s",
                                  "memberSince": "2020-01-01"
                                }
                                """.formatted(suffix, suffix)))
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
                                  "identification": "Match Team %s",
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
        String suffix = String.valueOf(System.nanoTime());
        String response = mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Match Trainer %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "1988-04-20",
                                  "email": "match-trainer-%s@club.com",
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

    private double count(String name) {
        Counter counter = meterRegistry.find(name).counter();
        return counter == null ? 0.0 : counter.count();
    }
}
