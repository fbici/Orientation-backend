package com.orientation.orientationapp.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntelligenceIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /intelligence/knowledge - Recherche dans le Knowledge Graph")
    void searchKnowledge_ReturnsResults() throws Exception {
        mockMvc.perform(get("/intelligence/knowledge").param("q", "informatique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
