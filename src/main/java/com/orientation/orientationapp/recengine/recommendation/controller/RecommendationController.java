package com.orientation.orientationapp.recengine.recommendation.controller;

import com.orientation.orientationapp.modules.university.entity.Program;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.recengine.eligibility.engine.EligibilityEngine;
import com.orientation.orientationapp.recengine.eligibility.model.EligibilityResult;
import com.orientation.orientationapp.recengine.profile.analyzer.impl.DefaultProfileAnalyzer;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.recommendation.dto.request.GenerateRecommendationRequest;
import com.orientation.orientationapp.recengine.recommendation.dto.request.SimulationRequest;
import com.orientation.orientationapp.recengine.recommendation.dto.response.RecommendationResponse;
import com.orientation.orientationapp.recengine.recommendation.dto.response.SimulationResponse;
import com.orientation.orientationapp.recengine.recommendation.engine.HistoricalEngine;
import com.orientation.orientationapp.recengine.ranking.engine.impl.DefaultRankingEngine;
import com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation;
import com.orientation.orientationapp.recengine.scoring.engine.impl.RealScoringEngine;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.simulation.engine.impl.DefaultSimulationEngine;
import com.orientation.orientationapp.recengine.simulation.model.SimulationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final DefaultProfileAnalyzer profileAnalyzer;
    private final RealScoringEngine scoringEngine;
    private final DefaultRankingEngine rankingEngine;
    private final DefaultSimulationEngine simulationEngine;
    private final EligibilityEngine eligibilityEngine;
    private final HistoricalEngine historicalEngine;
    private final ProgramRepository programRepository;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<RecommendationResponse> generateRecommendations(
            @Valid @RequestBody GenerateRecommendationRequest request) {

        log.info("Generating recommendations for candidate: {}", request.getCandidateId());

        // Build academic profile
        AcademicProfile profile = profileAnalyzer.analyze(
                request.getBacType(),
                request.getBacAverage(),
                request.getSubjectGrades()
        );

        if (request.getPreferredFields() != null) profile.setPreferredFields(request.getPreferredFields());
        if (request.getPreferredCountries() != null) profile.setPreferredCountries(request.getPreferredCountries());
        if (request.getLanguage() != null) profile.setLanguage(request.getLanguage());
        if (request.getBudget() != null) profile.setBudget(request.getBudget());

        // Get real programs from database
        List<Program> programs = programRepository.findByAvailableTrue();

        // Compute scores using real data
        List<CompositeScore> scores = new ArrayList<>();
        for (Program program : programs) {
            scores.add(scoringEngine.computeScore(profile, program.getId(), null));
        }

        // Rank
        List<RankedRecommendation> ranked = rankingEngine.rankWithExplanations(scores,
                programs.stream().map(p -> Map.<String, Object>of(
                        "id", p.getId().toString(),
                        "name", p.getName(),
                        "university", p.getFaculty().getCampus().getUniversity().getName(),
                        "faculty", p.getFaculty().getName()
                )).collect(Collectors.toList()));

        // Build response
        List<RecommendationResponse.RecommendationItem> items = ranked.stream()
                .map(this::mapToItem)
                .toList();

        return ResponseEntity.ok(RecommendationResponse.builder()
                .recommendations(items)
                .totalPrograms(items.size())
                .profileSummary("Profil analysé avec " + programs.size() + " programmes évalués")
                .build());
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<SimulationResponse> simulate(
            @Valid @RequestBody SimulationRequest request) {

        log.info("Running simulation for candidate: {}", request.getCandidateId());

        // Build original profile
        AcademicProfile originalProfile = profileAnalyzer.analyze(null, null, null);
        List<Program> programs = programRepository.findByAvailableTrue();

        List<CompositeScore> originalScores = new ArrayList<>();
        for (Program program : programs) {
            originalScores.add(scoringEngine.computeScore(originalProfile, program.getId(), null));
        }
        List<RankedRecommendation> originalRecommendations = rankingEngine.rank(originalScores,
                programs.stream().map(p -> Map.<String, Object>of("name", p.getName())).collect(Collectors.toList()));

        // Run simulation
        com.orientation.orientationapp.recengine.simulation.model.SimulationScenario scenario =
                com.orientation.orientationapp.recengine.simulation.model.SimulationScenario.builder()
                        .candidateId(request.getCandidateId())
                        .modifiedGrades(request.getModifiedGrades())
                        .modifiedBacType(request.getModifiedBacType())
                        .modifiedBacAverage(request.getModifiedBacAverage())
                        .modifiedCountry(request.getModifiedCountry())
                        .modifiedBudget(request.getModifiedBudget())
                        .modifiedLanguage(request.getModifiedLanguage())
                        .description(request.getDescription())
                        .build();

        SimulationResult simResult = simulationEngine.simulate(scenario, originalRecommendations);

        List<SimulationResponse.RecommendationItem> simItems = simResult.getSimulatedRecommendations().stream()
                .map(rec -> SimulationResponse.RecommendationItem.builder()
                        .rank(rec.getRank())
                        .programName(rec.getProgramName())
                        .universityName(rec.getUniversityName())
                        .score(rec.getScore().getFinalScore())
                        .build())
                .toList();

        List<SimulationResponse.SimulationDifference> diffs = simResult.getDifferences().stream()
                .map(d -> SimulationResponse.SimulationDifference.builder()
                        .type(d.getType())
                        .programName(d.getProgramName())
                        .description(d.getDescription())
                        .rankChange(d.getRankChange())
                        .build())
                .toList();

        return ResponseEntity.ok(SimulationResponse.builder()
                .simulatedRecommendations(simItems)
                .differences(diffs)
                .summary(simResult.getSummary())
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Map<String, Object>> list(Pageable pageable) {
        List<Program> programs = programRepository.findByAvailableTrue();
        Map<String, Object> response = new HashMap<>();
        response.put("programs", programs.size());
        response.put("message", "Programmes disponibles");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalPrograms", programRepository.findByAvailableTrue().size());
        dashboard.put("message", "Dashboard des recommandations");
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getDashboardStatistics() {
        return ResponseEntity.ok(Map.of("message", "Statistiques du dashboard"));
    }

    @GetMapping("/{id}/scores")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Map<String, String>> getScores(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("message", "Scores pour " + id));
    }

    @GetMapping("/{id}/explanation")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Map<String, String>> getExplanation(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("message", "Explication pour " + id));
    }

    private RecommendationResponse.RecommendationItem mapToItem(RankedRecommendation rec) {
        return RecommendationResponse.RecommendationItem.builder()
                .rank(rec.getRank())
                .programName(rec.getProgramName())
                .universityName(rec.getUniversityName())
                .facultyName(rec.getFacultyName())
                .score(rec.getScore().getFinalScore())
                .confidence(rec.getConfidence())
                .admissionProbability(rec.getAdmissionProbability())
                .difficultyLevel(rec.getDifficultyLevel())
                .eligible(rec.isEligible())
                .explanationSummary(rec.getExplanation() != null ? rec.getExplanation().getSummary() : "")
                .strengths(rec.getExplanation() != null ?
                        rec.getExplanation().getStrengths().stream().map(s -> s.getText()).toList() : List.of())
                .warnings(rec.getExplanation() != null ?
                        rec.getExplanation().getWarnings().stream().map(w -> w.getText()).toList() : List.of())
                .build();
    }
}
