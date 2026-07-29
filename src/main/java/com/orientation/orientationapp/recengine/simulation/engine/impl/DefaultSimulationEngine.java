package com.orientation.orientationapp.recengine.simulation.engine.impl;

import com.orientation.orientationapp.recengine.profile.analyzer.impl.DefaultProfileAnalyzer;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.ranking.engine.impl.DefaultRankingEngine;
import com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation;
import com.orientation.orientationapp.recengine.scoring.engine.impl.DefaultScoringEngine;
import com.orientation.orientationapp.recengine.simulation.model.SimulationResult;
import com.orientation.orientationapp.recengine.simulation.model.SimulationScenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultSimulationEngine {

    private final DefaultProfileAnalyzer profileAnalyzer;
    private final DefaultScoringEngine scoringEngine;
    private final DefaultRankingEngine rankingEngine;

    public SimulationResult simulate(SimulationScenario scenario, List<RankedRecommendation> originalRecommendations) {
        log.info("Running simulation for candidate: {}", scenario.getCandidateId());

        // Build modified profile
        AcademicProfile modifiedProfile = buildModifiedProfile(scenario);

        // Generate new recommendations
        List<RankedRecommendation> simulatedRecommendations = generateRecommendations(modifiedProfile);

        // Compute differences
        List<SimulationResult.SimulationDifference> differences = computeDifferences(
                originalRecommendations, simulatedRecommendations);

        String summary = buildSimulationSummary(differences, scenario);

        return SimulationResult.builder()
                .originalRecommendations(originalRecommendations)
                .simulatedRecommendations(simulatedRecommendations)
                .differences(differences)
                .summary(summary)
                .build();
    }

    private AcademicProfile buildModifiedProfile(SimulationScenario scenario) {
        Map<String, BigDecimal> grades = scenario.getModifiedGrades() != null ?
                new HashMap<>(scenario.getModifiedGrades()) : new HashMap<>();

        AcademicProfile profile = profileAnalyzer.analyze(
                scenario.getModifiedBacType(),
                scenario.getModifiedBacAverage(),
                grades
        );

        if (scenario.getModifiedCountry() != null) {
            profile.setPreferredCountries(List.of(scenario.getModifiedCountry()));
        }
        if (scenario.getModifiedBudget() != null) {
            profile.setBudget(scenario.getModifiedBudget());
        }
        if (scenario.getModifiedLanguage() != null) {
            profile.setLanguage(scenario.getModifiedLanguage());
        }

        return profile;
    }

    private List<RankedRecommendation> generateRecommendations(AcademicProfile profile) {
        // Generate mock program data for simulation
        List<Map<String, Object>> programs = generateMockPrograms();
        List<com.orientation.orientationapp.recengine.scoring.model.CompositeScore> scores = new ArrayList<>();

        for (Map<String, Object> program : programs) {
            scores.add(scoringEngine.computeScore(profile, program));
        }

        return rankingEngine.rank(scores, programs);
    }

    private List<Map<String, Object>> generateMockPrograms() {
        return List.of(
                Map.of("name", "Licence Informatique", "university", "UM5", "faculty", "Sciences"),
                Map.of("name", "Licence Mathématiques", "university", "UH2C", "faculty", "Sciences"),
                Map.of("name", "Licence Physique", "university", "UCA", "faculty", "Sciences"),
                Map.of("name", "Licence Droit", "university", "USMBA", "faculty", "Droit"),
                Map.of("name", "Licence Médecine", "university", "UH2C", "faculty", "Médecine")
        );
    }

    private List<SimulationResult.SimulationDifference> computeDifferences(
            List<RankedRecommendation> original, List<RankedRecommendation> simulated) {
        List<SimulationResult.SimulationDifference> differences = new ArrayList<>();

        Map<String, Integer> originalRanks = new HashMap<>();
        for (RankedRecommendation rec : original) {
            originalRanks.put(rec.getProgramName(), rec.getRank());
        }

        for (RankedRecommendation sim : simulated) {
            Integer originalRank = originalRanks.get(sim.getProgramName());
            if (originalRank != null) {
                int change = sim.getRank() - originalRank;
                if (change != 0) {
                    differences.add(SimulationResult.SimulationDifference.builder()
                            .type(change > 0 ? "IMPROVED" : "DECLINED")
                            .programName(sim.getProgramName())
                            .description("Rang changé de " + originalRank + " à " + sim.getRank())
                            .rankChange(change)
                            .build());
                }
            } else {
                differences.add(SimulationResult.SimulationDifference.builder()
                        .type("NEW")
                        .programName(sim.getProgramName())
                        .description("Nouveau programme dans les recommandations")
                        .rankChange(0)
                        .build());
            }
        }

        return differences;
    }

    private String buildSimulationSummary(List<SimulationResult.SimulationDifference> differences, SimulationScenario scenario) {
        if (differences.isEmpty()) {
            return "Aucun changement détecté après simulation";
        }

        long improved = differences.stream().filter(d -> "IMPROVED".equals(d.getType())).count();
        long declined = differences.stream().filter(d -> "DECLINED".equals(d.getType())).count();
        long newPrograms = differences.stream().filter(d -> "NEW".equals(d.getType())).count();

        return String.format("Simulation: %d amélioration(s), %d déclin(s), %d nouveau(x) programme(s)",
                improved, declined, newPrograms);
    }
}
