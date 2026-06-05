package com.clubmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

final class ControllerTestAuth {

    private ControllerTestAuth() {
    }

    static String loginToken(MockMvc mockMvc) throws Exception {
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
