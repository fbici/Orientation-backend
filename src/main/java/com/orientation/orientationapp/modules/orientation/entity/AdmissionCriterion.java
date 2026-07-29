package com.orientation.orientationapp.modules.orientation.entity;

import com.orientation.orientationapp.common.enums.CriterionOperator;
import com.orientation.orientationapp.common.enums.CriterionType;
import com.orientation.orientationapp.common.enums.LogicalOperator;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.university.entity.Faculty;
import com.orientation.orientationapp.modules.university.entity.Program;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "admission_criteria", uniqueConstraints = {
        @UniqueConstraint(name = "uk_admission_criterion_program_type", columnNames = {"guide_version_id", "program_id", "criterion_type", "subject_id"})
})
public class AdmissionCriterion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guide_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_admission_criterion_guide_version"))
    private GuideVersion guideVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", foreignKey = @ForeignKey(name = "fk_admission_criterion_program"))
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", foreignKey = @ForeignKey(name = "fk_admission_criterion_faculty"))
    private Faculty faculty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CriterionType criterionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CriterionOperator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", foreignKey = @ForeignKey(name = "fk_admission_criterion_subject"))
    private Subject subject;

    @Column(precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(length = 200)
    private String stringValue;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String logicalGroup = "DEFAULT";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    @Builder.Default
    private LogicalOperator logicalOperator = LogicalOperator.AND;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 1;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mandatory = true;
}
