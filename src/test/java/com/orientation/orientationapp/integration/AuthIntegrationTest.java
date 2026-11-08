package com.orientation.orientationapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired com.orientation.orientationapp.modules.auth.repository.UserRepository users;
    @Autowired com.orientation.orientationapp.modules.auth.repository.TenantRepository tenants;
    @Autowired com.orientation.orientationapp.modules.auth.repository.OrganizationRepository organizations;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;

    @Test
    @DisplayName("GET /auth/me - Profil utilisateur authentifie")
    void getMe_Authenticated() throws Exception {
        var org=new com.orientation.orientationapp.modules.auth.entity.Organization();
        org.setName("Auth integration fixture");org.setCode(java.util.UUID.randomUUID().toString());
        organizations.saveAndFlush(org);
        var tenant=new com.orientation.orientationapp.modules.auth.entity.Tenant();
        tenant.setName("Auth fixture");tenant.setCode(java.util.UUID.randomUUID().toString());tenant.setOrganization(org);
        tenants.saveAndFlush(tenant);
        var user=new com.orientation.orientationapp.modules.auth.entity.User();
        user.setEmail(java.util.UUID.randomUUID()+"@example.test");user.setFirstName("Auth");user.setLastName("Fixture");
        user.setTenant(tenant);user.setEmailVerified(true);user.setPassword(encoder.encode("Fixture-only-password-42!"));
        users.saveAndFlush(user);
        var login=mockMvc.perform(post("/auth/login").contentType("application/json")
            .content(objectMapper.writeValueAsString(java.util.Map.of("email",user.getEmail(),"password","Fixture-only-password-42!"))))
            .andExpect(status().isOk()).andReturn();
        var token=objectMapper.readTree(login.getResponse().getContentAsString()).path("accessToken").asText();
        mockMvc.perform(get("/auth/me").header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    @DisplayName("GET /auth/me - Non authentifie retourne 401")
    void getMe_Unauthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
