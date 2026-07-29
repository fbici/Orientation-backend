package com.orientation.orientationapp.recengine;

import com.orientation.orientationapp.recengine.profile.analyzer.impl.DefaultProfileAnalyzer;
import com.orientation.orientationapp.recengine.ranking.engine.impl.DefaultRankingEngine;
import com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation;
import com.orientation.orientationapp.recengine.scoring.engine.impl.DefaultScoringEngine;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.simulation.engine.impl.DefaultSimulationEngine;
import com.orientation.orientationapp.recengine.simulation.model.SimulationResult;
import com.orientation.orientationapp.recengine.simulation.model.SimulationScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private DefaultSimulationEngine simulationEngine;

    @BeforeEach
    void setUp() {
        simulationEngine = new DefaultSimulationEngine(
                new DefaultProfileAnalyzer(),
                new DefaultScoringEngine(),
                new DefaultRankingEngine()
        );
    }

    @Test
    void shouldRunSimulation() {
        SimulationScenario scenario = SimulationScenario.builder()
                .candidateId(UUID.randomUUID())
                .modifiedBacAverage(BigDecimal.valueOf(16))
                .modifiedGrades(Map.of("Mathématiques", BigDecimal.valueOf(18)))
                .description("Test simulation")
                .build();

        List<RankedRecommendation> original = List.of(
                RankedRecommendation.builder()
                        .programName("Program A")
                        .rank(1)
                        .score(CompositeScore.builder().finalScore(BigDecimal.valueOf(70)).build())
                        .build()
        );

        SimulationResult result = simulationEngine.simulate(scenario, original);

        assertNotNull(result);
        assertNotNull(result.getSimulatedRecommendations());
        assertNotNull(result.getDifferences());
        assertNotNull(result.getSummary());
    }
}
