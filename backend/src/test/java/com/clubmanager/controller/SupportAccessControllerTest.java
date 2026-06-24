package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:support-access-controller-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class SupportAccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSupportAccesses_AfterCreate_ReturnsCreatorAdminWithoutLazyLoadingFailure() throws Exception {
        String token = loginToken(mockMvc);
        String email = "support-%s@example.com".formatted(System.nanoTime());

        mockMvc.perform(post("/api/v1/support-access")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.createdByAdminUuid").exists())
                .andExpect(jsonPath("$.createdByAdminName").exists());

        mockMvc.perform(get("/api/v1/support-access?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", hasItem(email)))
                .andExpect(jsonPath("$.content[*].createdByAdminUuid").isNotEmpty())
                .andExpect(jsonPath("$.content[*].createdByAdminName").isNotEmpty());
    }
}
