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
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTrainer_WithValidToken_ReturnsCreatedTrainer() throws Exception {
        mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTrainerJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.name").value("Carlos Mendes"))
                .andExpect(jsonPath("$.registerDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void createTrainer_WithBlankName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTrainerJson().replace("Carlos Mendes", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createTrainer_WithFutureMemberSince_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTrainerJson().replace("2018-06-01", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createTrainer_WithFutureBirthdate_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTrainerJson().replace("1988-04-20", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createTrainer_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTrainerJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTrainers_WithValidToken_ReturnsPaginatedList() throws Exception {
        String trainerName = "Carlos Mendes List";
        String uuid = createTrainer(trainerName);

        mockMvc.perform(get("/api/v1/trainers")
                        .param("name", trainerName)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").isString())
                .andExpect(jsonPath("$.content[*].uuid", hasItem(uuid)))
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void getAllTrainers_WithActiveFilter_ReturnsOnlyActiveTrainers() throws Exception {
        String token = loginToken(mockMvc);
        String suffix = String.valueOf(System.nanoTime());
        String activeName = "Active Trainer " + suffix;
        String inactiveName = "Inactive Trainer " + suffix;
        createTrainer(activeName);
        String inactiveUuid = createTrainer(inactiveName);

        mockMvc.perform(patch("/api/v1/trainers/{uuid}/deactivate", inactiveUuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/trainers")
                        .param("active", "true")
                        .param("name", suffix)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem(activeName)))
                .andExpect(jsonPath("$.content[*].name", not(hasItem(inactiveName))));
    }

    @Test
    void getAllTrainers_WithInactiveFilter_ReturnsOnlyInactiveTrainers() throws Exception {
        String token = loginToken(mockMvc);
        String suffix = String.valueOf(System.nanoTime());
        String activeName = "Active Trainer " + suffix;
        String inactiveName = "Inactive Trainer " + suffix;
        createTrainer(activeName);
        String inactiveUuid = createTrainer(inactiveName);

        mockMvc.perform(patch("/api/v1/trainers/{uuid}/deactivate", inactiveUuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/trainers")
                        .param("active", "false")
                        .param("name", suffix)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem(inactiveName)))
                .andExpect(jsonPath("$.content[*].name", not(hasItem(activeName))));
    }

    @Test
    void getTrainerByUuid_WithValidToken_ReturnsFullTrainer() throws Exception {
        String uuid = createTrainer();

        mockMvc.perform(get("/api/v1/trainers/{uuid}", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid))
                .andExpect(jsonPath("$.email").value("carlos@club.com"))
                .andExpect(jsonPath("$.age").isNumber())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void getTrainerByUuid_WithUnknownUuid_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/{uuid}", "00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainer_WithValidRequest_ReturnsUpdatedTrainer() throws Exception {
        String uuid = createTrainer();

        mockMvc.perform(put("/api/v1/trainers/{uuid}", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana Mendes",
                                  "email": "ana@club.com",
                                  "phone": "555-2000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Mendes"))
                .andExpect(jsonPath("$.email").value("ana@club.com"))
                .andExpect(jsonPath("$.phone").value("555-2000"));
    }

    @Test
    void deactivateTrainer_WithValidToken_ReturnsInactiveTrainer() throws Exception {
        String uuid = createTrainer();

        mockMvc.perform(patch("/api/v1/trainers/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void reactivateTrainer_WithValidToken_ReturnsActiveTrainer() throws Exception {
        String uuid = createTrainer();

        mockMvc.perform(patch("/api/v1/trainers/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/trainers/{uuid}/reactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    private String createTrainer() throws Exception {
        return createTrainer("Carlos Mendes");
    }

    private String createTrainer(String name) throws Exception {
        String response = mockMvc.perform(post("/api/v1/trainers")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTrainerJson(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.uuid");
    }

    private String validTrainerJson() {
        return validTrainerJson("Carlos Mendes");
    }

    private String validTrainerJson(String name) {
        return """
                {
                  "name": "%s",
                  "birthCountry": "Brazil",
                  "livingCountry": "Brazil",
                  "birthdate": "1988-04-20",
                  "email": "carlos@club.com",
                  "phone": "555-0100",
                  "memberSince": "2018-06-01"
                }
                """.formatted(name);
    }

}
