package com.clubmanager.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clubmanager.repository.ClubSetupRepository;
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
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClubSetupRepository clubSetupRepository;

    @Test
    void getClub_WithoutAuthentication_ReturnsClubData() throws Exception {
        mockMvc.perform(get("/api/v1/club"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void updateClub_AsAdmin_ReturnsUpdatedClub() throws Exception {
        mockMvc.perform(put("/api/v1/club")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "City FC",
                                  "description": "Youth club",
                                  "colour1": "#112233",
                                  "colour2": "#AABBCC"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("City FC"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void updateClub_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/v1/club")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "City FC",
                                  "description": "Youth club",
                                  "colour1": "#112233",
                                  "colour2": "#AABBCC"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateClub_WithInvalidColour_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/club")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "City FC",
                                  "description": "Youth club",
                                  "colour1": "blue",
                                  "colour2": "#AABBCC"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void getAllSetup_AsAdmin_ReturnsSetupEntries() throws Exception {
        mockMvc.perform(get("/api/v1/club/setup")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").isString())
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void getSetupByType_AsAdmin_ReturnsSetupEntry() throws Exception {
        mockMvc.perform(get("/api/v1/club/setup/EVALUATION_LEVEL")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EVALUATION_LEVEL"));
    }

    @Test
    void updateSetup_AsAdmin_ReturnsUpdatedValues() throws Exception {
        String uuid = clubSetupRepository.findByType("EVALUATION_LEVEL").orElseThrow().getUuid().toString();

        mockMvc.perform(put("/api/v1/club/setup/{uuid}", uuid)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jsonData": "[\\"Beginner\\",\\"Advanced\\"]"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonData").value("[\"Beginner\",\"Advanced\"]"));
    }

    @Test
    void updateSetup_WithDuplicateValue_ReturnsBadRequest() throws Exception {
        String uuid = clubSetupRepository.findByType("EVALUATION_LEVEL").orElseThrow().getUuid().toString();

        mockMvc.perform(put("/api/v1/club/setup/{uuid}", uuid)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jsonData": "[\\"Advanced\\",\\"Advanced\\"]"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
