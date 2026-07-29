package com.orientation.orientationapp.modules.university.entity;

import com.orientation.orientationapp.common.enums.UniversityStatus;
import com.orientation.orientationapp.common.model.BaseEntity;
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
@Table(name = "universities", uniqueConstraints = {
        @UniqueConstraint(name = "uk_university_name_country", columnNames = {"name", "country_id"})
})
public class University extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String shortName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_university_country"))
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_university_city"))
    private City city;

    @Column(length = 500)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 500)
    private String website;

    private Integer foundedYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UniversityStatus status = UniversityStatus.ACTIVE;

    private Integer ranking;

    private Integer internationalRanking;

    private Integer studentCount;

    @Column(precision = 5, scale = 2)
    private BigDecimal acceptanceRate;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String logoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "university", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Campus> campuses = new HashSet<>();
}
