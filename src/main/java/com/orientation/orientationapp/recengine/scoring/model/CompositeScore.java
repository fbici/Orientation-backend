package com.orientation.orientationapp.recengine.scoring.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompositeScore {

    private BigDecimal finalScore;
    private BigDecimal confidence;
    private List<ScoreDetail> components;
    private String summary;
}
