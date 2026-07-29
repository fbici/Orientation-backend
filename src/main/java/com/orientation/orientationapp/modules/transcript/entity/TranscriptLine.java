package com.orientation.orientationapp.modules.transcript.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.orientation.entity.Subject;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transcript_lines", uniqueConstraints = {
        @UniqueConstraint(name = "uk_transcript_line_subject_semester", columnNames = {"transcript_id", "subject_id", "semester"})
})
public class TranscriptLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transcript_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transcript_line_transcript"))
    private Transcript transcript;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transcript_line_subject"))
    private Subject subject;

    @Column(nullable = false, length = 20)
    private String rawGrade;

    @Column(precision = 5, scale = 2)
    private BigDecimal rawValue;

    @Column(precision = 5, scale = 2)
    private BigDecimal normalizedValue;

    @Column(precision = 3, scale = 2)
    private BigDecimal coefficient;

    private Integer credits;

    private Integer semester;

    @Column(nullable = false)
    @Builder.Default
    private Boolean passed = false;

    @Column(length = 500)
    private String comment;
}
