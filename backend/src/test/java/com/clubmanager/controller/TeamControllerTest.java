package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.hasItem;
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
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTeam_WithValidToken_ReturnsCreatedTeam() throws Exception {
        String trainerUuid = createTrainer();

        mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTeamJson(trainerUuid)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.ageGroup").value("Under 13"))
                .andExpect(jsonPath("$.teamCategory").value("MASCULINE"))
                .andExpect(jsonPath("$.trainerUuid").value(trainerUuid))
                .andExpect(jsonPath("$.trainerName").value("Carlos Mendes"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void createTeam_WithBlankAgeGroup_ReturnsBadRequest() throws Exception {
        String trainerUuid = createTrainer();

        mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTeamJson(trainerUuid).replace("Under 13", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createTeam_WithUnknownTrainer_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTeamJson("00000000-0000-0000-0000-000000000000")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTeam_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTeamJson("00000000-0000-0000-0000-000000000000")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTeams_WithValidToken_ReturnsPaginatedList() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);

        mockMvc.perform(get("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").isString())
                .andExpect(jsonPath("$.content[*].uuid", hasItem(teamUuid)))
                .andExpect(jsonPath("$.content[0].trainerUuid").isString())
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void getTeamByUuid_WithValidToken_ReturnsFullTeam() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);

        mockMvc.perform(get("/api/v1/teams/{uuid}", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(teamUuid))
                .andExpect(jsonPath("$.trainerUuid").value(trainerUuid))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void getTeamByUuid_WithUnknownUuid_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/teams/{uuid}", "00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTeam_WithValidRequest_ReturnsUpdatedTeam() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);

        mockMvc.perform(put("/api/v1/teams/{uuid}", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ageGroup": "Under 15",
                                  "teamCategory": "FEMININE",
                                  "trainerUuid": "%s"
                                }
                                """.formatted(trainerUuid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ageGroup").value("Under 15"))
                .andExpect(jsonPath("$.teamCategory").value("FEMININE"));
    }

    @Test
    void deactivateTeam_WithValidToken_ReturnsInactiveTeam() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);

        mockMvc.perform(patch("/api/v1/teams/{uuid}/deactivate", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void reactivateTeam_WithValidToken_ReturnsActiveTeam() throws Exception {
        String trainerUuid = createTrainer();
        String teamUuid = createTeam(trainerUuid);

        mockMvc.perform(patch("/api/v1/teams/{uuid}/deactivate", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/teams/{uuid}/reactivate", teamUuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    private String createTeam(String trainerUuid) throws Exception {
        String response = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTeamJson(trainerUuid)))
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

    private String validTeamJson(String trainerUuid) {
        return """
                {
                  "ageGroup": "Under 13",
                  "teamCategory": "MASCULINE",
                  "trainerUuid": "%s"
                }
                """.formatted(trainerUuid);
    }
}

