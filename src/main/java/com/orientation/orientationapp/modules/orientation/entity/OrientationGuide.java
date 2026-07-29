package com.orientation.orientationapp.modules.orientation.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.university.entity.Country;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orientation_guides", uniqueConstraints = {
        @UniqueConstraint(name = "uk_orientation_guide_country_year", columnNames = {"country_id", "academic_year_id"})
})
public class OrientationGuide extends BaseEntity {

    @Column(nullable = false, length = 300)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orientation_guide_country"))
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orientation_guide_academic_year"))
    private AcademicYear academicYear;

    @Column(length = 200)
    private String publisher;

    private LocalDate publicationDate;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String documentUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "orientationGuide", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<GuideVersion> versions = new HashSet<>();
}
