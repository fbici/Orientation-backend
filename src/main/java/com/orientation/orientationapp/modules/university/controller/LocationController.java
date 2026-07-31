package com.orientation.orientationapp.modules.university.controller;

import com.orientation.orientationapp.modules.university.entity.City;
import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.modules.university.repository.CityRepository;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Pays et villes pour les formulaires dynamiques")
public class LocationController {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @Operation(summary = "Liste des pays", description = "Retourne tous les pays actifs triés par nom")
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
            map.put("phoneCode", c.getPhoneCode());
            map.put("currency", c.getCurrency());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Villes d'un pays", description = "Retourne les villes d'un pays donné")
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
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Toutes les villes", description = "Retourne toutes les villes, filtrable par pays")
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
