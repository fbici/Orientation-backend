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
                Map.of("id", java.util.UUID.nameUUIDFromBytes("Program A".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "name", "Program A"),
                Map.of("id", java.util.UUID.nameUUIDFromBytes("Program B".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "name", "Program B"),
                Map.of("id", java.util.UUID.nameUUIDFromBytes("Program C".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "name", "Program C")
        );

        List<RankedRecommendation> ranked = rankingEngine.rank(scores, programs);

        assertNotNull(ranked);
        assertEquals(3, ranked.size());
        assertEquals(1, ranked.get(0).getRank());
        assertEquals("Program B", ranked.get(0).getProgramName());
    }

    @Test
    void shouldNeverInventAdmissionDifficultyOrProbability() {
        List<CompositeScore> scores = List.of(
                createScore(BigDecimal.valueOf(90)),
                createScore(BigDecimal.valueOf(50)),
                createScore(BigDecimal.valueOf(30))
        );

        List<Map<String, Object>> programs = List.of(
                Map.of("id", java.util.UUID.nameUUIDFromBytes("Easy".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "name", "Easy"),
                Map.of("id", java.util.UUID.nameUUIDFromBytes("Medium".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "name", "Medium"),
                Map.of("id", java.util.UUID.nameUUIDFromBytes("Hard".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "name", "Hard")
        );

        List<RankedRecommendation> ranked = rankingEngine.rank(scores, programs);

        assertNull(ranked.get(0).getDifficultyLevel());
        assertNull(ranked.get(0).getAdmissionProbability());
        assertFalse(ranked.get(0).isEligible());
        assertNull(ranked.get(1).getDifficultyLevel());
        assertNull(ranked.get(1).getAdmissionProbability());
        assertFalse(ranked.get(1).isEligible());
        assertNull(ranked.get(2).getDifficultyLevel());
        assertNull(ranked.get(2).getAdmissionProbability());
        assertFalse(ranked.get(2).isEligible());
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
    @Test void compatibilityCannotOverrideEligibility() {
        var idA=java.util.UUID.randomUUID();var idB=java.util.UUID.randomUUID();
        var eligible=com.orientation.orientationapp.recengine.eligibility.model.EligibilityStatus.ELIGIBLE;
        var refused=com.orientation.orientationapp.recengine.eligibility.model.EligibilityStatus.NOT_ELIGIBLE;
        var ranked=rankingEngine.rank(List.of(createScore(BigDecimal.TEN),createScore(BigDecimal.valueOf(99))),
            List.of(Map.of("id",idA,"eligibility",eligible),Map.of("id",idB,"eligibility",refused)));
        assertEquals(idA,ranked.getFirst().getProgramId());assertTrue(ranked.getFirst().isEligible());
        assertEquals(refused,ranked.getLast().getEligibilityStatus());assertFalse(ranked.getLast().isEligible());
    }
}
