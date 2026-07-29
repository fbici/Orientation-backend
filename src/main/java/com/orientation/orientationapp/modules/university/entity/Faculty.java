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
@Table(name = "faculties", uniqueConstraints = {
        @UniqueConstraint(name = "uk_faculty_name_campus", columnNames = {"name", "campus_id"})
})
public class Faculty extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campus_id", nullable = false, foreignKey = @ForeignKey(name = "fk_faculty_campus"))
    private Campus campus;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 100)
    private String deanName;

    @Column(length = 255)
    private String email;

    @Column(length = 500)
    private String website;

    private Integer capacity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "faculty", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Program> programs = new HashSet<>();
}
