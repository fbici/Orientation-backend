package com.orientation.orientationapp.recengine.ranking.model;

import com.orientation.orientationapp.recengine.explanation.model.Explanation;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankedRecommendation {

    private UUID programId;
    private String programName;
    private String universityName;
    private String facultyName;
    private Integer rank;
    private CompositeScore score;
    private BigDecimal confidence;
    private BigDecimal admissionProbability;
    private String difficultyLevel;
    private Explanation explanation;
    private boolean eligible;
}
