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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UniversityIntegrationTest {

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
                            .content(mapper.writeValueAsString(req)))
                    .andReturn();
            if (result.getResponse().getStatus() == 200) {
                adminToken = mapper.readTree(result.getResponse().getContentAsString())
                        .get("accessToken").asText();
            }
        } catch (Exception e) {
            // Admin peut ne pas exister dans le test DB
        }
    }

    @Test
    @DisplayName("GET /universities - Liste paginee")
    void listUniversities_ReturnsPage() throws Exception {
        mockMvc.perform(get("/api/v1/universities").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @DisplayName("GET /universities - Contient des universites du seed")
    void listUniversities_ContainsSeededData() throws Exception {
        String response = mockMvc.perform(get("/api/v1/universities").param("size", "100"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Verifier que les universites seedees sont presentes
        boolean hasUAC = response.contains("Abomey-Calavi") || response.contains("UAC");
        boolean hasMohammed = response.contains("Mohammed V") || response.contains("UM5");
        boolean hasUCAD = response.contains("Cheikh Anta Diop") || response.contains("UCAD");

        assert hasUAC || hasMohammed || hasUCAD :
                "Les universites seedees doivent etre presentes dans la reponse";
    }

    @Test
    @DisplayName("GET /universities/{id} - Detail d'une universite")
    void getUniversity_ReturnsDetail() throws Exception {
        // Recuperer une universite
        String listJson = mockMvc.perform(get("/api/v1/universities").param("size", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String uniId = listJson.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        if (uniId != null && !uniId.equals(listJson)) {
            mockMvc.perform(get("/api/v1/universities/" + uniId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").exists());
        }
    }
}
