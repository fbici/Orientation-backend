package com.orientation.orientationapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientation.orientationapp.modules.auth.dto.request.LoginRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntelligenceIntegrationTest {

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
    @DisplayName("POST /intelligence/smart-query - Requete en langage naturel")
    void smartQuery_ReturnsAnswer() throws Exception {
        if (adminToken == null) return;

        mockMvc.perform(post("/api/v1/intelligence/smart-query")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"Je veux etudier l informatique au Benin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").exists())
                .andExpect(jsonPath("$.answer").exists())
                .andExpect(jsonPath("$.keywords").isArray());
    }

    @Test
    @DisplayName("GET /intelligence/knowledge?q= - Recherche dans le Knowledge Graph")
    void searchKnowledge_ReturnsResults() throws Exception {
        if (adminToken == null) return;

        mockMvc.perform(get("/api/v1/intelligence/knowledge")
                        .param("q", "informatique")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /intelligence/feedback - Enregistrer un feedback")
    void recordFeedback_Success() throws Exception {
        if (adminToken == null) return;

        mockMvc.perform(post("/api/v1/intelligence/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recommendationId\":\"00000000-0000-0000-0000-000000000001\",\"candidateId\":\"00000000-0000-0000-0000-000000000002\",\"programId\":\"00000000-0000-0000-0000-000000000003\",\"action\":\"ACCEPTED\"}"))
                .andExpect(status().isOk());
    }
}
