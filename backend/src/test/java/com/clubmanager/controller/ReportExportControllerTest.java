package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exportPlayers_WithValidToken_ReturnsCsvWithoutInternalIds() throws Exception {
        createPlayer("CSV Player, One", "CSV-PLAYER-1");

        mockMvc.perform(get("/api/v1/reports/players.csv")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("players.csv")))
                .andExpect(content().string(containsString("Name,Age,Team Category,Skill Level,Positions,Registration Number,Member Since,Status")))
                .andExpect(content().string(containsString("\"CSV Player, One\"")))
                .andExpect(content().string(containsString("CSV-PLAYER-1")))
                .andExpect(content().string(not(containsString("id"))));
    }

    @Test
    void exportTeamRoster_WithValidToken_ReturnsActiveRosterCsvWithoutInternalIds() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);
        String playerUuid = createPlayer("Roster Player", "CSV-ROSTER-1");

        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerUuid": "%s",
                                  "jerseyNumber": 10
                                }
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reports/teams/{teamUuid}/roster.csv", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("team-roster.csv")))
                .andExpect(content().string(containsString("Team,Number,Player,Age,Team Category,Skill Level,Positions,Assigned Date")))
                .andExpect(content().string(containsString("Under 13 A MASCULINE,10,Roster Player")))
                .andExpect(content().string(not(containsString("id"))));
    }

    @Test
    void exportPlayers_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reports/players.csv"))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportSchedules_WithValidToken_ReturnsCsvWithoutInternalIds() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);
        String fieldUuid = getFirstFieldUuid();
        createSchedule(teamUuid, fieldUuid);

        mockMvc.perform(get("/api/v1/reports/schedules.csv")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("schedules.csv")))
                .andExpect(content().string(containsString("Team,Field,Date Time,Duration Minutes,Type,Status,Notes,Cancel Reason")))
                .andExpect(content().string(containsString("Main Field")))
                .andExpect(content().string(containsString("TRAINING")))
                .andExpect(content().string(not(containsString("id"))));
    }

    @Test
    void exportChampionships_WithValidToken_ReturnsCsvWithoutInternalIds() throws Exception {
        String teamUuid = createTeam(createTrainer());
        createChampionship(teamUuid);

        mockMvc.perform(get("/api/v1/reports/championships.csv")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("championships.csv")))
                .andExpect(content().string(containsString("Name,Team,Period,Expected Matches,Status,Description")))
                .andExpect(content().string(containsString("CSV Championship")))
                .andExpect(content().string(not(containsString("id"))));
    }

    @Test
    void exportEvaluationResults_WithValidToken_ReturnsAssignedPlayersAndResultsWithoutInternalIds() throws Exception {
        String playerUuid = createPlayer("Evaluation Export Player", "CSV-EVAL-1");
        String evaluationUuid = createEvaluation();
        assignEvaluationPlayer(evaluationUuid, playerUuid);
        String eventUuid = createEvaluationEvent(evaluationUuid);
        startEvaluation(evaluationUuid);
        updateAttendance(eventUuid, playerUuid);
        completeEvaluationEvent(eventUuid);
        updateEvaluationResult(evaluationUuid, playerUuid);

        mockMvc.perform(get("/api/v1/reports/evaluations/{evaluationUuid}/results.csv", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("evaluation-results.csv")))
                .andExpect(content().string(containsString("Evaluation,Group,Status,Player,Participation,Final Skill Level,Source Event,Finalized At")))
                .andExpect(content().string(containsString("Evaluation Export Player")))
                .andExpect(content().string(containsString("DEBUTANT")))
                .andExpect(content().string(not(containsString("id"))));
    }

    @Test
    void exportMatchAnalysis_WithValidToken_ReturnsRosterAnalysisWithoutInternalIds() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);
        String playerUuid = createPlayer("Match Export Player", "CSV-MATCH-1");
        assignTeamPlayer(teamUuid, playerUuid);
        String matchUuid = createMatch(teamUuid);
        saveMatchAnalysis(teamUuid, matchUuid, playerUuid);

        mockMvc.perform(get("/api/v1/reports/teams/{teamUuid}/matches/{matchUuid}/analysis.csv", teamUuid, matchUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("match-analysis.csv")))
                .andExpect(content().string(containsString("Team,Opponent,Place,Match Date Time,Score,Championship,Player,Positions,Improvement Opportunities,Highlights,Notes")))
                .andExpect(content().string(containsString("Match Export Player")))
                .andExpect(content().string(containsString("Improve pass")))
                .andExpect(content().string(containsString("Good passes")))
                .andExpect(content().string(not(containsString("id"))));
    }

    private String createPlayer(String name, String registrationNumber) throws Exception {
        String response = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "2013-03-15",
                                  "teamCategory": "MASCULINE",
                                  "positions": ["MIDFIELD"],
                                  "registrationNumber": "%s",
                                  "memberSince": "2020-01-01"
                                }
                                """.formatted(name, registrationNumber)))
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

    private void createSchedule(String teamUuid, String fieldUuid) throws Exception {
        mockMvc.perform(post("/api/v1/schedules")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teamUuid": "%s",
                                  "fieldUuid": "%s",
                                  "dateTime": "2026-09-10T18:00:00",
                                  "durationMinutes": 90,
                                  "type": "TRAINING",
                                  "notes": "CSV schedule"
                                }
                                """.formatted(teamUuid, fieldUuid)))
                .andExpect(status().isCreated());
    }

    private void createChampionship(String teamUuid) throws Exception {
        mockMvc.perform(post("/api/v1/championships")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CSV Championship",
                                  "description": "CSV export championship",
                                  "teamUuid": "%s",
                                  "startMonth": 1,
                                  "startYear": 2026,
                                  "endMonth": 12,
                                  "endYear": 2026,
                                  "expectedMatches": 20
                                }
                                """.formatted(teamUuid)))
                .andExpect(status().isCreated());
    }

    private String createEvaluation() throws Exception {
        String response = mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "CSV Evaluation",
                                  "ageGroup": "Under 13",
                                  "teamCategory": "MASCULINE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private void assignEvaluationPlayer(String evaluationUuid, String playerUuid) throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/{evaluationUuid}/players", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s"}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated());
    }

    private String createEvaluationEvent(String evaluationUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/evaluations/{evaluationUuid}/events", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "place": "CSV Field",
                                  "eventDate": "2026-09-12",
                                  "startTime": "18:00",
                                  "durationMinutes": 60
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private void startEvaluation(String evaluationUuid) throws Exception {
        mockMvc.perform(patch("/api/v1/evaluations/{evaluationUuid}/start", evaluationUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk());
    }

    private void updateAttendance(String eventUuid, String playerUuid) throws Exception {
        mockMvc.perform(put("/api/v1/evaluation-events/{eventUuid}/attendance/{playerUuid}", eventUuid, playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PRESENT"}
                                """))
                .andExpect(status().isOk());
    }

    private void completeEvaluationEvent(String eventUuid) throws Exception {
        mockMvc.perform(patch("/api/v1/evaluation-events/{eventUuid}/complete", eventUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk());
    }

    private void updateEvaluationResult(String evaluationUuid, String playerUuid) throws Exception {
        mockMvc.perform(put("/api/v1/evaluations/{evaluationUuid}/results/{playerUuid}", evaluationUuid, playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"levelResult": "DEBUTANT"}
                                """))
                .andExpect(status().isOk());
    }

    private void assignTeamPlayer(String teamUuid, String playerUuid) throws Exception {
        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s", "jerseyNumber": 10}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated());
    }

    private String createMatch(String teamUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams/{teamUuid}/matches", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opponent": "CSV Rivals",
                                  "place": "Main Field",
                                  "matchDateTime": "2026-09-13T18:00:00",
                                  "teamScore": 2,
                                  "opponentScore": 1,
                                  "notes": "CSV match"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private void saveMatchAnalysis(String teamUuid, String matchUuid, String playerUuid) throws Exception {
        mockMvc.perform(put("/api/v1/teams/{teamUuid}/matches/{matchUuid}/players/{playerUuid}", teamUuid, matchUuid, playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "improvementTags": ["Improve pass"],
                                  "highlightTags": ["Good passes"],
                                  "notes": "CSV analysis note"
                                }
                                """))
                .andExpect(status().isOk());
    }

    private String createTeam(String trainerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identification": "Under 13 A",
                                  "ageCategory": "U13",
                                  "teamCategory": "MASCULINE",
                                  "trainerUuid": "%s"
                                }
                                """.formatted(trainerUuid)))
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
                                  "name": "CSV Trainer",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "1988-04-20",
                                  "email": "csv.trainer@club.com",
                                  "phone": "555-0140",
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
