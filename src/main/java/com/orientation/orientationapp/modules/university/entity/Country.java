package com.orientation.orientationapp.modules.university.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "countries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_country_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_country_name", columnNames = "name")
})
public class Country extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 3)
    private String code;

    @Column(length = 200)
    private String officialName;

    @Column(length = 10)
    private String phoneCode;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<City> cities = new HashSet<>();

    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<University> universities = new HashSet<>();
}
