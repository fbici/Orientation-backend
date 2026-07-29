package com.orientation.orientationapp.recengine.profile.analyzer.impl;

import com.orientation.orientationapp.recengine.profile.analyzer.ProfileAnalyzer;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DefaultProfileAnalyzer implements ProfileAnalyzer {

    @Override
    public AcademicProfile analyze(UUID candidateId) {
        log.info("Analyzing profile for candidate: {}", candidateId);
        // In production, this would load from database
        return AcademicProfile.builder()
                .candidateId(candidateId)
                .build();
    }

    @Override
    public AcademicProfile analyze(String bacType, BigDecimal bacAverage, Map<String, BigDecimal> subjectGrades) {
        AcademicProfile profile = AcademicProfile.builder()
                .bacType(bacType)
                .bacAverage(bacAverage)
                .subjectGrades(subjectGrades != null ? subjectGrades : new HashMap<>())
                .build();

        // Compute derived metrics
        if (subjectGrades != null && !subjectGrades.isEmpty()) {
            profile.setGeneralAverage(computeAverage(subjectGrades));
            profile.setStrongSubjects(findStrongSubjects(subjectGrades));
            profile.setWeakSubjects(findWeakSubjects(subjectGrades));
            profile.setCompetencyScores(computeCompetencies(subjectGrades));
            profile.setDominantCompetency(findDominantCompetency(profile.getCompetencyScores()));
            profile.setNormalizedScore(normalizeScore(bacAverage));
        }

        return profile;
    }

    private BigDecimal computeAverage(Map<String, BigDecimal> grades) {
        if (grades.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = grades.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
    }

    private List<String> findStrongSubjects(Map<String, BigDecimal> grades) {
        return grades.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.valueOf(15)) >= 0)
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> findWeakSubjects(Map<String, BigDecimal> grades) {
        return grades.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.valueOf(10)) < 0)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> computeCompetencies(Map<String, BigDecimal> grades) {
        Map<String, BigDecimal> competencies = new HashMap<>();

        // Analytical (Math, Physics, Logic)
        BigDecimal analytical = computeCategoryAverage(grades, List.of("Mathématiques", "Physique", "Logique"));
        competencies.put("analytical", analytical);

        // Linguistic (Arabic, French, English)
        BigDecimal linguistic = computeCategoryAverage(grades, List.of("Arabe", "Français", "Anglais"));
        competencies.put("linguistic", linguistic);

        // Scientific (Physics, Chemistry, SVT)
        BigDecimal scientific = computeCategoryAverage(grades, List.of("Physique", "Chimie", "SVT", "Sciences"));
        competencies.put("scientific", scientific);

        // Humanistic (History, Geography, Philosophy)
        BigDecimal humanistic = computeCategoryAverage(grades, List.of("Histoire", "Géographie", "Philosophie"));
        competencies.put("humanistic", humanistic);

        return competencies;
    }

    private BigDecimal computeCategoryAverage(Map<String, BigDecimal> grades, List<String> categorySubjects) {
        List<BigDecimal> matching = grades.entrySet().stream()
                .filter(e -> categorySubjects.stream().anyMatch(s -> e.getKey().toLowerCase().contains(s.toLowerCase())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        if (matching.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = matching.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(matching.size()), 2, RoundingMode.HALF_UP);
    }

    private String findDominantCompetency(Map<String, BigDecimal> competencies) {
        return competencies.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private BigDecimal normalizeScore(BigDecimal average) {
        if (average == null) return BigDecimal.ZERO;
        // Normalize to 0-100 scale
        return average.multiply(BigDecimal.valueOf(5)).setScale(2, RoundingMode.HALF_UP);
    }
}
