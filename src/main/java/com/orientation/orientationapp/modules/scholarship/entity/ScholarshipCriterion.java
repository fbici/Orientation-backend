package com.orientation.orientationapp.modules.scholarship.entity;

import com.orientation.orientationapp.common.enums.CriterionOperator;
import com.orientation.orientationapp.common.enums.CriterionType;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.orientation.entity.GuideVersion;
import com.orientation.orientationapp.modules.orientation.entity.Subject;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scholarship_criteria", uniqueConstraints = {
        @UniqueConstraint(name = "uk_scholarship_criterion_type", columnNames = {"guide_version_id", "scholarship_id", "criterion_type", "subject_id"})
})
public class ScholarshipCriterion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guide_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_scholarship_criterion_guide_version"))
    private GuideVersion guideVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scholarship_id", nullable = false, foreignKey = @ForeignKey(name = "fk_scholarship_criterion_scholarship"))
    private Scholarship scholarship;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CriterionType criterionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CriterionOperator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", foreignKey = @ForeignKey(name = "fk_scholarship_criterion_subject"))
    private Subject subject;

    @Column(precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(length = 200)
    private String stringValue;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mandatory = true;
}
