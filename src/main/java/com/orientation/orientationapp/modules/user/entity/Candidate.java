package com.orientation.orientationapp.modules.user.entity;

import com.orientation.orientationapp.common.enums.BacMention;
import com.orientation.orientationapp.common.enums.BacType;
import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.common.enums.Gender;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.university.entity.City;
import com.orientation.orientationapp.modules.university.entity.Country;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "candidates")
public class Candidate extends BaseEntity {

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_candidate_country"))
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", foreignKey = @ForeignKey(name = "fk_candidate_city"))
    private City city;

    @Column(length = 200)
    private String highSchool;

    @Column(nullable = false)
    private Integer bacYear;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BacType bacType;

    @Column(precision = 5, scale = 2)
    private BigDecimal bacAverage;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BacMention bacMention;

    @Column(length = 500)
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CandidateStatus status = CandidateStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    private Instant lastLoginAt;
}
