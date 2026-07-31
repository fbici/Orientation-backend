package com.orientation.orientationapp.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /locations/countries - Retourne la liste des pays")
    void getCountries_ReturnsList() throws Exception {
        String response = mockMvc.perform(get("/api/v1/locations/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains("Benin") || response.contains("Maroc") || response.contains("France"),
                "La reponse doit contenir au moins un pays");
    }

    @Test
    @DisplayName("GET /locations/countries - Chaque pays a id, name, code")
    void getCountries_HasCorrectStructure() throws Exception {
        mockMvc.perform(get("/api/v1/locations/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].code").exists());
    }

    @Test
    @DisplayName("GET /locations/cities - Retourne la liste des villes")
    void getCities_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/v1/locations/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /locations/cities?countryId= - Filtre par pays")
    void getCities_FilteredByCountry() throws Exception {
        // D'abord recuperer un pays
        String countriesJson = mockMvc.perform(get("/api/v1/locations/countries"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extraire le premier ID de pays
        String countryId = countriesJson.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        if (countryId != null && !countryId.equals(countriesJson)) {
            mockMvc.perform(get("/api/v1/locations/cities").param("countryId", countryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }
}
