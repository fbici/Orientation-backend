package com.orientation.orientationapp.recengine;

import com.orientation.orientationapp.recengine.ranking.engine.impl.DefaultRankingEngine;
import com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.scoring.model.ScoreDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RankingEngineTest {

    private DefaultRankingEngine rankingEngine;

    @BeforeEach
    void setUp() {
        rankingEngine = new DefaultRankingEngine();
    }

    @Test
    void shouldRankProgramsByScore() {
        List<CompositeScore> scores = List.of(
                createScore(BigDecimal.valueOf(60)),
                createScore(BigDecimal.valueOf(80)),
                createScore(BigDecimal.valueOf(70))
        );

        List<Map<String, Object>> programs = List.of(
                Map.of("name", "Program A"),
                Map.of("name", "Program B"),
                Map.of("name", "Program C")
        );

        List<RankedRecommendation> ranked = rankingEngine.rank(scores, programs);

        assertNotNull(ranked);
        assertEquals(3, ranked.size());
        assertEquals(1, ranked.get(0).getRank());
        assertEquals("Program B", ranked.get(0).getProgramName());
    }

    @Test
    void shouldAssignDifficultyLevel() {
        List<CompositeScore> scores = List.of(
                createScore(BigDecimal.valueOf(90)),
                createScore(BigDecimal.valueOf(50)),
                createScore(BigDecimal.valueOf(30))
        );

        List<Map<String, Object>> programs = List.of(
                Map.of("name", "Easy"),
                Map.of("name", "Medium"),
                Map.of("name", "Hard")
        );

        List<RankedRecommendation> ranked = rankingEngine.rank(scores, programs);

        assertEquals("Facile", ranked.get(0).getDifficultyLevel());
        assertEquals("Difficile", ranked.get(1).getDifficultyLevel());
        assertEquals("Très difficile", ranked.get(2).getDifficultyLevel());
    }

    private CompositeScore createScore(BigDecimal value) {
        return CompositeScore.builder()
                .finalScore(value)
                .confidence(BigDecimal.valueOf(80))
                .components(List.of(
                        ScoreDetail.builder()
                                .scoreType("academic")
                                .score(value)
                                .weight(BigDecimal.valueOf(0.3))
                                .explanation("Test")
                                .computed(true)
                                .build()
                ))
                .summary("Test score")
                .build();
    }
}
