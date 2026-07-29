package com.orientation.orientationapp.knowledge.smartquery.service.impl;
import java.util.List;
import java.util.Map;

import com.orientation.orientationapp.knowledge.smartquery.service.SmartQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DefaultSmartQueryService implements SmartQueryService {

    private static final Map<String, String> QUERY_PATTERNS = Map.of(
            "universitÃƒÂ©", "universities",
            "programme", "programs",
            "bourse", "scholarships",
            "moyenne", "average",
            "admission", "admission_criteria",
            "math", "subjects",
            "canada", "countries",
            "informatique", "programs"
    );

    @Override
    public Map<String, Object> processQuery(String query) {
        log.info("Processing smart query: {}", query);

        String lowerQuery = query.toLowerCase();
        Map<String, Object> result = new HashMap<>();

        // Detect query intent
        String intent = detectIntent(lowerQuery);
        result.put("query", query);
        result.put("intent", intent);

        // Extract entities
        Map<String, String> entities = extractEntities(lowerQuery);
        result.put("entities", entities);

        // Generate response
        String response = generateResponse(intent, entities, lowerQuery);
        result.put("response", response);
        result.put("confidence", 0.75);

        return result;
    }

    @Override
    public Map<String, Object> getSuggestions(String partial) {
        List<String> suggestions = new ArrayList<>();

        if (partial.toLowerCase().contains("univ")) {
            suggestions.addAll(List.of(
                    "Quelles universitÃƒÂ©s acceptent une moyenne de 14 ?",
                    "Quelle est la meilleure universitÃƒÂ© au Maroc ?",
                    "Compare les universitÃƒÂ©s de Casablanca"
            ));
        } else if (partial.toLowerCase().contains("program")) {
            suggestions.addAll(List.of(
                    "Quels programmes en informatique ?",
                    "Compare Licence et Master en Informatique",
                    "Quelles filiÃƒÂ¨res avec un bac scientifique ?"
            ));
        } else if (partial.toLowerCase().contains("bours")) {
            suggestions.addAll(List.of(
                    "Quelles bourses existent au Maroc ?",
                    "Bourses pour une moyenne de 16",
                    "Bourses gouvernementales disponibles"
            ));
        } else {
            suggestions.addAll(List.of(
                    "OÃƒÂ¹ puis-je ÃƒÂ©tudier avec 13 de moyenne ?",
                    "Quelle universitÃƒÂ© accepte un Bac D ?",
                    "Quels programmes exigent les mathÃƒÂ©matiques ?"
            ));
        }

        return Map.of("suggestions", suggestions);
    }

    private String detectIntent(String query) {
        if (query.contains("universit") || query.contains("facult")) return "UNIVERSITY_SEARCH";
        if (query.contains("program") || query.contains("filiÃƒÂ¨re")) return "PROGRAM_SEARCH";
        if (query.contains("bourse") || query.contains("financement")) return "SCHOLARSHIP_SEARCH";
        if (query.contains("moyenne") || query.contains("note")) return "AVERAGE_QUERY";
        if (query.contains("compar")) return "COMPARISON";
        if (query.contains("admission") || query.contains("critÃƒÂ¨re")) return "ADMISSION_QUERY";
        return "GENERAL_SEARCH";
    }

    private Map<String, String> extractEntities(String query) {
        Map<String, String> entities = new HashMap<>();

        // Extract numbers (averages)
        String[] words = query.split("\\s+");
        for (String word : words) {
            try {
                double value = Double.parseDouble(word);
                entities.put("average", String.valueOf(value));
            } catch (NumberFormatException ignored) {}
        }

        // Extract keywords
        for (Map.Entry<String, String> pattern : QUERY_PATTERNS.entrySet()) {
            if (query.contains(pattern.getKey())) {
                entities.put(pattern.getKey(), pattern.getValue());
            }
        }

        return entities;
    }

    private String generateResponse(String intent, Map<String, String> entities, String query) {
        return switch (intent) {
            case "UNIVERSITY_SEARCH" -> "Je recherche les universitÃƒÂ©s correspondant ÃƒÂ  votre demande...";
            case "PROGRAM_SEARCH" -> "Voici les programmes disponibles pour votre profil...";
            case "SCHOLARSHIP_SEARCH" -> "Voici les bourses correspondant ÃƒÂ  vos critÃƒÂ¨res...";
            case "AVERAGE_QUERY" -> "Analyse de votre moyenne par rapport aux critÃƒÂ¨res d'admission...";
            case "COMPARISON" -> "Comparaison en cours...";
            default -> "Analyse de votre requÃƒÂªte en cours...";
        };
    }
}
