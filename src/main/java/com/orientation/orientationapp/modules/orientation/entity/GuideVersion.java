package com.orientation.orientationapp.modules.orientation.entity;

import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.scholarship.entity.ScholarshipCriterion;
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
@Table(name = "guide_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_guide_version_number", columnNames = {"orientation_guide_id", "version_number"})
})
public class GuideVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orientation_guide_id", nullable = false, foreignKey = @ForeignKey(name = "fk_guide_version_guide"))
    private OrientationGuide orientationGuide;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(length = 50)
    private String versionLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GuideVersionStatus status = GuideVersionStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "guideVersion", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AdmissionCriterion> admissionCriteria = new HashSet<>();

    @OneToMany(mappedBy = "guideVersion", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ScholarshipCriterion> scholarshipCriteria = new HashSet<>();
}
