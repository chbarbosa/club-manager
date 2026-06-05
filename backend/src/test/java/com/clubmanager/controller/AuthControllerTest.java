package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_WithValidCredentials_ReturnsTokenResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token", not("")))
                .andExpect(jsonPath("$.adminUuid").isString())
                .andExpect(jsonPath("$.name").value("Admin"));
    }

    @Test
    void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("BAD_CREDENTIALS"));
    }

    @Test
    void login_WithInactiveAdmin_ReturnsUnauthorized() throws Exception {
        String token = loginToken(mockMvc);
        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Inactive Admin",
                                  "email": "inactive@club.com",
                                  "username": "inactive",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isCreated());

        String uuid = com.jayway.jsonpath.JsonPath.read(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "inactive", "password": "secret1"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), "$.adminUuid");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admins/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "inactive", "password": "secret1"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithMissingFields_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "", "password": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithValidToken_ReturnsCreatedAdmin() throws Exception {
        String token = loginToken(mockMvc);

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane Admin",
                                  "email": "jane@club.com",
                                  "username": "jane",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.name").value("Jane Admin"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void register_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane Admin",
                                  "email": "jane@club.com",
                                  "username": "jane",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_WithDuplicateUsername_ReturnsBadRequest() throws Exception {
        String token = loginToken(mockMvc);

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Another Admin",
                                  "email": "another@club.com",
                                  "username": "admin",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

}
