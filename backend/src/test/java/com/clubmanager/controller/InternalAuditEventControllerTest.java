package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clubmanager.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "app.audit.internal-api.enabled=true",
        "app.audit.internal-api.allowed-cidrs=127.0.0.1/32"
})
@AutoConfigureMockMvc
@Transactional
class InternalAuditEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void getAuditEvents_WithAllowedInternalAdmin_ReturnsPaginatedUuidOnlyResponse() throws Exception {
        createPlayer("Audit Player", "AUDIT-PLAYER-1");

        mockMvc.perform(get("/internal/api/v1/audit-events")
                        .param("entityType", "PLAYER")
                        .param("action", "CREATED")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].action", hasItem("CREATED")))
                .andExpect(jsonPath("$.content[*].entityType", hasItem("PLAYER")))
                .andExpect(jsonPath("$.content[*].entityLabel", hasItem("Audit Player")))
                .andExpect(jsonPath("$.content[0].uuid").isString())
                .andExpect(jsonPath("$.content[0].actorAdminUuid").isString())
                .andExpect(jsonPath("$.content[0].id").doesNotExist())
                .andExpect(jsonPath("$.content[0].actorAdminId").doesNotExist());
    }

    @Test
    void getAuditEvents_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/internal/api/v1/audit-events"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuditEvents_FromOutsideAllowedCidr_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/internal/api/v1/audit-events")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicAuditEndpoint_DoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/audit-events")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isNotFound());
    }

    @Test
    void failedMutation_DoesNotCreateAuditEvent() throws Exception {
        long before = auditEventRepository.count();

        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson("", "AUDIT-INVALID-1")))
                .andExpect(status().isBadRequest());

        org.assertj.core.api.Assertions.assertThat(auditEventRepository.count()).isEqualTo(before);
    }

    private void createPlayer(String name, String registrationNumber) throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + loginToken(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPlayerJson(name, registrationNumber)))
                .andExpect(status().isCreated());
    }

    private String validPlayerJson(String name, String registrationNumber) {
        return """
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
                """.formatted(name, registrationNumber);
    }
}
