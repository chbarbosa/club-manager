package com.clubmanager.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class SupportReadOnlySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void supportUser_CanReadOperationalEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/players")
                        .with(user("support@example.com").roles("SUPPORT")))
                .andExpect(status().isOk());
    }

    @Test
    void supportUser_CannotCreateOperationalData() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .with(user("support@example.com").roles("SUPPORT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportUser_CannotMutateOperationalData() throws Exception {
        mockMvc.perform(patch("/api/v1/players/00000000-0000-0000-0000-000000000000/deactivate")
                        .with(user("support@example.com").roles("SUPPORT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportUser_CannotReadAdminOnlyAreas() throws Exception {
        mockMvc.perform(get("/api/v1/admins")
                        .with(user("support@example.com").roles("SUPPORT")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/support-access")
                        .with(user("support@example.com").roles("SUPPORT")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/club-analysis")
                        .with(user("support@example.com").roles("SUPPORT")))
                .andExpect(status().isForbidden());
    }

    private String validPlayerJson() {
        return """
                {
                  "name": "Support Security Player",
                  "birthCountry": "Brazil",
                  "livingCountry": "Brazil",
                  "birthdate": "2008-03-15",
                  "teamCategory": "MASCULINE",
                  "positions": ["MIDFIELD"],
                  "registrationNumber": "SUPPORT-SECURITY-%s",
                  "memberSince": "%s"
                }
                """.formatted(System.nanoTime(), LocalDate.now().minusYears(1));
    }
}
