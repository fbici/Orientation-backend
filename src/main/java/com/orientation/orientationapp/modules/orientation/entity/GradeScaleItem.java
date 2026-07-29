package com.orientation.orientationapp.modules.orientation.entity;

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
@Table(name = "grade_scale_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_grade_scale_item_raw", columnNames = {"grade_scale_id", "raw_grade"}),
        @UniqueConstraint(name = "uk_grade_scale_item_normalized", columnNames = {"grade_scale_id", "normalized_value"})
})
public class GradeScaleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_scale_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_scale_item_scale"))
    private GradeScale gradeScale;

    @Column(nullable = false, length = 10)
    private String rawGrade;

    @Column(precision = 5, scale = 2)
    private BigDecimal rawValue;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal normalizedValue;

    @Column(precision = 3, scale = 2)
    private BigDecimal normalizedGpa;

    @Column(length = 100)
    private String label;

    @Column(nullable = false)
    private Integer sortOrder;
}
