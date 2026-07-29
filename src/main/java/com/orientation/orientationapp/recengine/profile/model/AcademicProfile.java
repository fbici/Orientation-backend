package com.orientation.orientationapp.recengine.profile.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicProfile {

    private UUID candidateId;
    private String firstName;
    private String lastName;
    private String bacType;
    private String bacMention;
    private BigDecimal bacAverage;
    private Integer bacYear;
    private String country;
    private String city;

    // Grades
    private BigDecimal generalAverage;
    private BigDecimal weightedAverage;
    private Map<String, BigDecimal> subjectGrades;
    private List<String> strongSubjects;
    private List<String> weakSubjects;

    // Competencies
    private Map<String, BigDecimal> competencyScores;
    private String dominantCompetency;

    // Preferences
    private List<String> preferredFields;
    private List<String> preferredCountries;
    private List<String> preferredLanguages;
    private BigDecimal budget;
    private String language;

    // Computed
    private BigDecimal normalizedScore;
    private BigDecimal percentile;
    private Integer nationalRank;
}
