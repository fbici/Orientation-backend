package com.orientation.orientationapp.modules.scholarship.entity;

import com.orientation.orientationapp.common.enums.ScholarshipStatus;
import com.orientation.orientationapp.common.enums.ScholarshipType;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.orientation.entity.AcademicYear;
import com.orientation.orientationapp.modules.university.entity.Country;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scholarships", uniqueConstraints = {
        @UniqueConstraint(name = "uk_scholarship_name_country_year", columnNames = {"name", "country_id", "academic_year_id"})
})
public class Scholarship extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_scholarship_country"))
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_scholarship_academic_year"))
    private AcademicYear academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScholarshipType type;

    @Column(length = 200)
    private String provider;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String coverage;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    private Integer duration;

    private LocalDate deadline;

    @Column(length = 500)
    private String applicationUrl;

    private Integer totalSlots;

    private Integer remainingSlots;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ScholarshipStatus status = ScholarshipStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean government = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "scholarship", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ScholarshipCriterion> criteria = new HashSet<>();
}
