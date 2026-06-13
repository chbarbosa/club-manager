package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InternalAuditEventDisabledControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuditEvents_WhenInternalApiDisabled_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/internal/api/v1/audit-events")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isForbidden());
    }
}
