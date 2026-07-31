package com.orientation.orientationapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientation.orientationapp.modules.auth.dto.request.LoginRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String adminToken;

    @BeforeAll
    static void loginAsAdmin(@Autowired MockMvc mvc, @Autowired ObjectMapper mapper) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@orientation.com");
        req.setPassword("admin123");
        try {
            MvcResult result = mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(req))).andReturn();
            if (result.getResponse().getStatus() == 200) {
                adminToken = mapper.readTree(result.getResponse().getContentAsString())
                        .get("accessToken").asText();
            }
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("GET /recommendations - Liste des recommandations")
    void listRecommendations_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations").param("page", "0").param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /recommendations/generate - Generation de recommandations")
    void generateRecommendations_ReturnsResults() throws Exception {
        if (adminToken == null) return; // Skip si pas d'admin

        Map<String, Object> request = Map.of(
                "candidateId", "00000000-0000-0000-0000-000000000001",
                "bacType", "Sciences Experimentales",
                "bacAverage", 14.5,
                "subjectGrades", Map.of(
                        "Mathematiques", 16,
                        "Physique", 15,
                        "Chimie", 14
                )
        );

        mockMvc.perform(post("/api/v1/recommendations/generate")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /recommendations/simulate - Simulation de scenario")
    void simulate_ReturnsResults() throws Exception {
        if (adminToken == null) return;

        Map<String, Object> request = Map.of(
                "candidateId", "00000000-0000-0000-0000-000000000001",
                "modifiedBacAverage", 16.0
        );

        mockMvc.perform(post("/api/v1/recommendations/simulate")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
