package com.orientation.orientationapp.modules.recommendation.entity;

import com.orientation.orientationapp.common.enums.ExplanationType;
import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recommendation_explanations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_explanation_recommendation_criterion", columnNames = {"recommendation_id", "criterion"})
})
public class RecommendationExplanation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_explanation_recommendation"))
    private Recommendation recommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExplanationType type;

    @Column(nullable = false, length = 100)
    private String criterion;

    @Column(nullable = false, length = 100)
    private String expectedValue;

    @Column(nullable = false, length = 100)
    private String actualValue;

    @Column(nullable = false)
    @Builder.Default
    private Boolean met = false;

    @Column(precision = 5, scale = 2)
    private BigDecimal impact;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 1;
}
