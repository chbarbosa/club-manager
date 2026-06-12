package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                                  "playerUuid": "%s"
                                }
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reports/teams/{teamUuid}/roster.csv", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("team-roster.csv")))
                .andExpect(content().string(containsString("Team,Player,Age,Team Category,Skill Level,Positions,Assigned Date")))
                .andExpect(content().string(containsString("Under 13 A MASCULINE,Roster Player")))
                .andExpect(content().string(not(containsString("id"))));
    }

    @Test
    void exportPlayers_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reports/players.csv"))
                .andExpect(status().isForbidden());
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
