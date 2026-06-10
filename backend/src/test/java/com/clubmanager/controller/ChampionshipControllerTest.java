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
class ChampionshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createChampionship_WithValidToken_ReturnsCreatedChampionship() throws Exception {
        String teamUuid = createTeam();
        String name = "City Cup " + System.nanoTime();

        mockMvc.perform(post("/api/v1/championships")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validChampionshipJson(name, teamUuid)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.teamUuid").value(teamUuid))
                .andExpect(jsonPath("$.teamIdentification").isString())
                .andExpect(jsonPath("$.startMonth").value(4))
                .andExpect(jsonPath("$.endMonth").value(6))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void createChampionship_WithInvalidPeriod_ReturnsBadRequest() throws Exception {
        String teamUuid = createTeam();

        mockMvc.perform(post("/api/v1/championships")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Bad Cup",
                                  "teamUuid": "%s",
                                  "startMonth": 8,
                                  "startYear": 2026,
                                  "endMonth": 6,
                                  "endYear": 2026
                                }
                                """.formatted(teamUuid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Championship end period must be after the start period"));
    }

    @Test
    void getAllChampionships_WithNameFilter_ReturnsPaginatedList() throws Exception {
        String teamUuid = createTeam();
        String name = "City Cup " + System.nanoTime();
        String championshipUuid = createChampionship(name, teamUuid);

        mockMvc.perform(get("/api/v1/championships")
                        .param("name", name)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].uuid", hasItem(championshipUuid)))
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void rosterFlow_WithEligiblePlayer_AddsAndRemovesPlayer() throws Exception {
        String trainerUuid = createTrainer("Roster Trainer");
        String teamUuid = createTeam(trainerUuid);
        String playerUuid = createPlayer();
        assignPlayerToTeam(teamUuid, playerUuid);
        String championshipUuid = createChampionship("Roster Cup " + System.nanoTime(), teamUuid);

        String rosterUuid = assignRosterPlayer(championshipUuid, playerUuid, trainerUuid);

        mockMvc.perform(get("/api/v1/championships/{uuid}/roster", championshipUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].uuid", hasItem(rosterUuid)))
                .andExpect(jsonPath("$[0].playerUuid").value(playerUuid))
                .andExpect(jsonPath("$[0].trainerUuid").value(trainerUuid))
                .andExpect(jsonPath("$[0].id").doesNotExist());

        mockMvc.perform(delete("/api/v1/championships/{uuid}/roster/{rosterUuid}", championshipUuid, rosterUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(rosterUuid))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.removedDate").value(LocalDate.now().toString()));

        mockMvc.perform(get("/api/v1/championships/{uuid}/roster", championshipUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void assignRosterPlayer_WhenPlayerNotOnTeam_ReturnsBadRequest() throws Exception {
        String trainerUuid = createTrainer("Roster Trainer");
        String teamUuid = createTeam(trainerUuid);
        String playerUuid = createPlayer();
        String championshipUuid = createChampionship("Roster Cup " + System.nanoTime(), teamUuid);

        mockMvc.perform(post("/api/v1/championships/{uuid}/roster", championshipUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerUuid": "%s",
                                  "trainerUuid": "%s"
                                }
                                """.formatted(playerUuid, trainerUuid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Player must be assigned to the championship team roster"));
    }

    private String assignRosterPlayer(String championshipUuid, String playerUuid, String trainerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/championships/{uuid}/roster", championshipUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerUuid": "%s",
                                  "trainerUuid": "%s"
                                }
                                """.formatted(playerUuid, trainerUuid)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private void assignPlayerToTeam(String teamUuid, String playerUuid) throws Exception {
        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s"}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated());
    }

    private String createChampionship(String name, String teamUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/championships")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validChampionshipJson(name, teamUuid)))
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
                                  "name": "Championship Player %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "2013-03-15",
                                  "teamCategory": "MASCULINE",
                                  "positions": ["MIDFIELD"],
                                  "registrationNumber": "CHAMP-%s",
                                  "memberSince": "2020-01-01"
                                }
                                """.formatted(suffix, suffix)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String createTeam() throws Exception {
        return createTeam(createTrainer("Championship Trainer"));
    }

    private String createTeam(String trainerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identification": "Championship Team %s",
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

    private String createTrainer(String prefix) throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String response = mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "1988-04-20",
                                  "email": "championship-trainer-%s@club.com",
                                  "phone": "555-0100",
                                  "memberSince": "%s"
                                }
                                """.formatted(prefix, suffix, suffix, LocalDate.now().minusYears(5))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String validChampionshipJson(String name, String teamUuid) {
        return """
                {
                  "name": "%s",
                  "description": "Spring tournament",
                  "teamUuid": "%s",
                  "startMonth": 4,
                  "startYear": 2026,
                  "endMonth": 6,
                  "endYear": 2026
                }
                """.formatted(name, teamUuid);
    }
}
