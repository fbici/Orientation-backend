package com.orientation.orientationapp.modules.university.controller;

import com.orientation.orientationapp.modules.university.entity.City;
import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.modules.university.repository.CityRepository;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import com.orientation.orientationapp.modules.university.repository.FacultyRepository;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    /**
     * GET /api/v1/locations/countries
     * Retourne tous les pays actifs
     */
    @GetMapping("/countries")
    public ResponseEntity<List<Map<String, Object>>> getCountries() {
        List<Country> countries = countryRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .sorted(Comparator.comparing(Country::getName))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = countries.stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId().toString());
            map.put("name", c.getName());
            map.put("code", c.getCode());
            map.put("officialName", c.getOfficialName());
            map.put("phoneCode", c.getPhoneCode());
            map.put("currency", c.getCurrency());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/locations/countries/{countryId}/cities
     * Retourne les villes d'un pays
     */
    @GetMapping("/countries/{countryId}/cities")
    public ResponseEntity<List<Map<String, Object>>> getCitiesByCountry(@PathVariable UUID countryId) {
        List<City> cities = cityRepository.findByCountryId(countryId).stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .sorted(Comparator.comparing(City::getName))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = cities.stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId().toString());
            map.put("name", c.getName());
            map.put("countryId", c.getCountry().getId().toString());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/locations/cities
     * Retourne toutes les villes (optionnel: filtrer par pays)
     */
    @GetMapping("/cities")
    public ResponseEntity<List<Map<String, Object>>> getCities(
            @RequestParam(required = false) UUID countryId) {
        List<City> cities;
        if (countryId != null) {
            cities = cityRepository.findByCountryId(countryId);
        } else {
            cities = cityRepository.findAll();
        }

        cities = cities.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .sorted(Comparator.comparing(City::getName))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = cities.stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId().toString());
            map.put("name", c.getName());
            map.put("countryId", c.getCountry().getId().toString());
            map.put("countryName", c.getCountry().getName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
