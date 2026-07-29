package com.orientation.orientationapp.modules.university.entity;

import com.orientation.orientationapp.common.enums.ProgramType;
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
@Table(name = "programs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_program_name_faculty", columnNames = {"name", "faculty_id"})
})
public class Program extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false, foreignKey = @ForeignKey(name = "fk_program_faculty"))
    private Faculty faculty;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProgramType type;

    @Column(length = 100)
    private String degree;

    @Column(nullable = false)
    private Integer duration;

    @Column(length = 50)
    private String language;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String objectives;

    @Column(columnDefinition = "text")
    private String prerequisites;

    private Integer maxStudents;

    @Column(precision = 12, scale = 2)
    private BigDecimal tuitionFee;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(length = 500)
    private String website;
}
