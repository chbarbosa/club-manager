package com.clubmanager.controller;

import static com.clubmanager.controller.ControllerTestAuth.loginToken;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clubmanager.repository.AdminRepository;
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
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void getAllAdmins_WithValidToken_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/v1/admins")
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").isString())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void getAllAdmins_WithActiveFilter_ReturnsOnlyActiveAdmins() throws Exception {
        String token = loginToken(mockMvc);
        String suffix = String.valueOf(System.nanoTime());
        String activeUsername = "active-admin-" + suffix;
        String inactiveUsername = "inactive-admin-" + suffix;
        registerAdmin(token, activeUsername, activeUsername + "@club.com");
        registerAdmin(token, inactiveUsername, inactiveUsername + "@club.com");
        String inactiveUuid = adminRepository.findByUsername(inactiveUsername).orElseThrow().getUuid().toString();

        mockMvc.perform(patch("/api/v1/admins/{uuid}/deactivate", inactiveUuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admins")
                        .param("active", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem(activeUsername)))
                .andExpect(jsonPath("$[*].username", not(hasItem(inactiveUsername))));
    }

    @Test
    void getAllAdmins_WithoutToken_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admins"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdminByUuid_WithValidToken_ReturnsAdmin() throws Exception {
        String uuid = adminRepository.findByUsername("admin").orElseThrow().getUuid().toString();

        mockMvc.perform(get("/api/v1/admins/{uuid}", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void deactivateAdmin_WhenMultipleAdmins_ReturnsInactiveAdmin() throws Exception {
        String token = loginToken(mockMvc);
        registerAdmin(token, "jane", "jane@club.com");
        String uuid = adminRepository.findByUsername("jane").orElseThrow().getUuid().toString();

        mockMvc.perform(patch("/api/v1/admins/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deactivateAdmin_WhenLastActiveAdmin_ReturnsBadRequest() throws Exception {
        String uuid = adminRepository.findByUsername("admin").orElseThrow().getUuid().toString();

        mockMvc.perform(patch("/api/v1/admins/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + loginToken(mockMvc)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void reactivateAdmin_WhenInactive_ReturnsActiveAdmin() throws Exception {
        String token = loginToken(mockMvc);
        registerAdmin(token, "jane", "jane@club.com");
        String uuid = adminRepository.findByUsername("jane").orElseThrow().getUuid().toString();

        mockMvc.perform(patch("/api/v1/admins/{uuid}/deactivate", uuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/admins/{uuid}/reactivate", uuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    private void registerAdmin(String token, String username, String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane Admin",
                                  "email": "%s",
                                  "username": "%s",
                                  "password": "StrongPass1"
                                }
                                """.formatted(email, username)))
                .andExpect(status().isCreated());
    }

}
