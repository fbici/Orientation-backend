package com.orientation.orientationapp.modules.orientation.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.university.entity.Country;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grade_scales", uniqueConstraints = {
        @UniqueConstraint(name = "uk_grade_scale_country_year", columnNames = {"country_id", "academic_year_id"})
})
public class GradeScale extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_scale_country"))
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_scale_academic_year"))
    private AcademicYear academicYear;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal minScore;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal passingScore;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal normalizeTo;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "gradeScale", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<GradeScaleItem> items = new HashSet<>();
}
