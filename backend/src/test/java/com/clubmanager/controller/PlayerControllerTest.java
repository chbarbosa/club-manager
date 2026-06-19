package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPlayer_WithValidToken_ReturnsCreatedPlayer() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-100")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.name").value("Joao Silva"))
                .andExpect(jsonPath("$.positions[0]").value("MIDFIELD"))
                .andExpect(jsonPath("$.registerDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void createPlayer_WithBlankName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-101").replace("Joao Silva", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createPlayer_WithFutureBirthdate_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-102").replace("2005-03-15", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createPlayer_WithFutureMemberSince_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-103").replace("2020-01-01", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createPlayer_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-104")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPlayers_WithValidToken_ReturnsPaginatedList() throws Exception {
        String playerName = "Joao Silva REG-105";
        String uuid = createPlayer(playerName, "REG-105");

        mockMvc.perform(get("/api/v1/players")
                        .param("name", playerName)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").isString())
                .andExpect(jsonPath("$.content[*].uuid", hasItem(uuid)))
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void getAllPlayers_WithActiveFilter_ReturnsOnlyActivePlayers() throws Exception {
        String token = loginToken(mockMvc);
        String suffix = String.valueOf(System.nanoTime());
        String activeName = "Active Player " + suffix;
        String inactiveName = "Inactive Player " + suffix;
        createPlayer(activeName, "ACTIVE-" + suffix);
        String inactiveUuid = createPlayer(inactiveName, "INACTIVE-" + suffix);

        mockMvc.perform(patch("/api/v1/players/{uuid}/deactivate", inactiveUuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/players")
                        .param("active", "true")
                        .param("name", suffix)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem(activeName)))
                .andExpect(jsonPath("$.content[*].name", not(hasItem(inactiveName))));
    }

    @Test
    void getAllPlayers_WithInactiveFilter_ReturnsOnlyInactivePlayers() throws Exception {
        String token = loginToken(mockMvc);
        String suffix = String.valueOf(System.nanoTime());
        String activeName = "Active Player " + suffix;
        String inactiveName = "Inactive Player " + suffix;
        createPlayer(activeName, "ACTIVE-FALSE-" + suffix);
        String inactiveUuid = createPlayer(inactiveName, "INACTIVE-FALSE-" + suffix);

        mockMvc.perform(patch("/api/v1/players/{uuid}/deactivate", inactiveUuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/players")
                        .param("active", "false")
                        .param("name", suffix)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem(inactiveName)))
                .andExpect(jsonPath("$.content[*].name", not(hasItem(activeName))));
    }

    @Test
    void getPlayerByUuid_WithValidToken_ReturnsFullPlayer() throws Exception {
        String uuid = createPlayer("REG-106");

        mockMvc.perform(get("/api/v1/players/{uuid}", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid))
                .andExpect(jsonPath("$.birthCountry").value("Brazil"))
                .andExpect(jsonPath("$.age").isNumber())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void getPlayerSkillHistory_WithNoHistory_ReturnsEmptyList() throws Exception {
        String uuid = createPlayer("REG-111");

        mockMvc.perform(get("/api/v1/players/{uuid}/skill-history", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPlayerTeamHistory_WithAssignments_ReturnsTeamsAndChampionshipCount() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);
        String playerUuid = createPlayer("REG-112");
        String assignmentUuid = assignTeamPlayer(teamUuid, playerUuid);
        createChampionship(teamUuid);

        mockMvc.perform(get("/api/v1/players/{uuid}/teams", playerUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.championshipCount").value(1))
                .andExpect(jsonPath("$.teams[0].assignmentUuid").value(assignmentUuid))
                .andExpect(jsonPath("$.teams[0].teamUuid").value(teamUuid))
                .andExpect(jsonPath("$.teams[0].teamIdentification").value("Player History Team"))
                .andExpect(jsonPath("$.teams[0].jerseyNumber").value(10))
                .andExpect(jsonPath("$.teams[0].active").value(true))
                .andExpect(jsonPath("$.teams[0].id").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void getPlayerByUuid_WithUnknownUuid_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/players/{uuid}", "00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePlayer_WithValidRequest_ReturnsUpdatedPlayer() throws Exception {
        String uuid = createPlayer("REG-107");

        mockMvc.perform(put("/api/v1/players/{uuid}", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "livingCountry": "Canada",
                                  "registrationNumber": "REG-108"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.livingCountry").value("Canada"))
                .andExpect(jsonPath("$.registrationNumber").value("REG-108"));
    }

    @Test
    void deactivatePlayer_WithValidToken_ReturnsInactivePlayer() throws Exception {
        String uuid = createPlayer("REG-109");

        mockMvc.perform(patch("/api/v1/players/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void reactivatePlayer_WithValidToken_ReturnsActivePlayer() throws Exception {
        String uuid = createPlayer("REG-110");

        mockMvc.perform(patch("/api/v1/players/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/players/{uuid}/reactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    private String createPlayer(String registrationNumber) throws Exception {
        return createPlayer("Joao Silva", registrationNumber);
    }

    private String createPlayer(String name, String registrationNumber) throws Exception {
        String response = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson(name, registrationNumber)))
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
                                  "name": "Player History Trainer %s",
                                  "birthCountry": "Brazil",
                                  "livingCountry": "Brazil",
                                  "birthdate": "1988-04-20",
                                  "email": "player-history-trainer-%s@club.com",
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
                                  "identification": "Player History Team",
                                  "ageCategory": "U19_PLUS",
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

    private String assignTeamPlayer(String teamUuid, String playerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s", "jerseyNumber": 10}
                                """.formatted(playerUuid)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private void createChampionship(String teamUuid) throws Exception {
        mockMvc.perform(post("/api/v1/championships")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Player History Cup %s",
                                  "description": "Player history championship",
                                  "teamUuid": "%s",
                                  "startMonth": 4,
                                  "startYear": 2026,
                                  "endMonth": 6,
                                  "endYear": 2026,
                                  "expectedMatches": 8
                                }
                                """.formatted(System.nanoTime(), teamUuid)))
                .andExpect(status().isCreated());
    }

    private String validPlayerJson(String registrationNumber) {
        return validPlayerJson("Joao Silva", registrationNumber);
    }

    private String validPlayerJson(String name, String registrationNumber) {
        return """
                {
                  "name": "%s",
                  "birthCountry": "Brazil",
                  "livingCountry": "Brazil",
                  "birthdate": "2005-03-15",
                  "teamCategory": "MASCULINE",
                  "positions": ["MIDFIELD"],
                  "registrationNumber": "%s",
                  "memberSince": "2020-01-01"
                }
                """.formatted(name, registrationNumber);
    }

}
