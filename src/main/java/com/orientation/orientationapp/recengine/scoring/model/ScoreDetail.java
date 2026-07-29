package com.orientation.orientationapp.recengine.scoring.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreDetail {

    private String scoreType;
    private BigDecimal score;
    private BigDecimal weight;
    private String explanation;
    private boolean computed;
}
