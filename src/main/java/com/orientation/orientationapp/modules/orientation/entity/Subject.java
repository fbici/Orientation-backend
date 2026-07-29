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
@Table(name = "subjects", uniqueConstraints = {
        @UniqueConstraint(name = "uk_subject_name_scale", columnNames = {"name", "grade_scale_id"})
})
public class Subject extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_scale_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subject_grade_scale"))
    private GradeScale gradeScale;

    @Column(length = 50)
    private String category;

    @Column(precision = 3, scale = 2)
    private BigDecimal coefficient;

    @Column(nullable = false)
    @Builder.Default
    private Boolean core = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
