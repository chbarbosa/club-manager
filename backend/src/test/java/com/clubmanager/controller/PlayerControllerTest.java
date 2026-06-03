package com.clubmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;

import com.clubmanager.repository.PlayerRepository;
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

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void createPlayer_WithValidToken_ReturnsCreatedPlayer() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-100")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.name").value("Joao Silva"))
                .andExpect(jsonPath("$.registerDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void createPlayer_WithBlankName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-101").replace("Joao Silva", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createPlayer_WithFutureBirthdate_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("REG-102").replace("2005-03-15", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createPlayer_WithFutureMemberSince_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken())
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
        String uuid = createPlayer("REG-105");

        mockMvc.perform(get("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").isString())
                .andExpect(jsonPath("$.content[*].uuid", hasItem(uuid)))
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void getPlayerByUuid_WithValidToken_ReturnsFullPlayer() throws Exception {
        String uuid = createPlayer("REG-106");

        mockMvc.perform(get("/api/v1/players/{uuid}", uuid)
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid))
                .andExpect(jsonPath("$.birthCountry").value("Brazil"))
                .andExpect(jsonPath("$.age").isNumber())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void getPlayerByUuid_WithUnknownUuid_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/players/{uuid}", "00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePlayer_WithValidRequest_ReturnsUpdatedPlayer() throws Exception {
        String uuid = createPlayer("REG-107");

        mockMvc.perform(put("/api/v1/players/{uuid}", uuid)
                        .header("Authorization", "Bearer " + loginToken())
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
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void reactivatePlayer_WithValidToken_ReturnsActivePlayer() throws Exception {
        String uuid = createPlayer("REG-110");

        mockMvc.perform(patch("/api/v1/players/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/players/{uuid}/reactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    private String createPlayer(String registrationNumber) throws Exception {
        String response = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson(registrationNumber)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String validPlayerJson(String registrationNumber) {
        return """
                {
                  "name": "Joao Silva",
                  "birthCountry": "Brazil",
                  "livingCountry": "Brazil",
                  "birthdate": "2005-03-15",
                  "teamCategory": "MASCULINE",
                  "registrationNumber": "%s",
                  "memberSince": "2020-01-01"
                }
                """.formatted(registrationNumber);
    }

    private String loginToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return response.replaceFirst(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
