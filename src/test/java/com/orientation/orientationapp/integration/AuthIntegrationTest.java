package com.orientation.orientationapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientation.orientationapp.modules.auth.dto.request.LoginRequest;
import com.orientation.orientationapp.modules.auth.dto.request.RegisterRequest;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String testEmail = "test.integration@orientation.com";
    private static String testPassword = "TestPassword123!";
    private static String accessToken;

    @Test
    @Order(1)
    @DisplayName("POST /auth/register - Inscription candidat")
    void register_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .firstName("Jean")
                .lastName("Dupont")
                .phone("+229 97 00 00 00")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.message").value("Inscription reussie. Vous pouvez maintenant vous connecter."))
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());
    }

    @Test
    @Order(2)
    @DisplayName("POST /auth/register - Email deja utilise")
    void register_DuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .firstName("Jean")
                .lastName("Dupont")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cet email est deja utilise"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /auth/login - Connexion reussie")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value(testEmail))
                .andReturn();

        // Extraire le token pour les tests suivants
        String body = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(body).get("accessToken").asText();
        assertNotNull(accessToken);
    }

    @Test
    @Order(4)
    @DisplayName("POST /auth/login - Mot de passe incorrect")
    void login_WrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(5)
    @DisplayName("GET /auth/me - Profil utilisateur authentifie")
    void getMe_Authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.firstName").value("Jean"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /auth/me - Non authentifie")
    void getMe_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isForbidden());
    }
}
