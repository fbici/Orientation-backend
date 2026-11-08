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
@Builder(toBuilder=true)
public class AcademicProfile {

    private UUID candidateId;
    private UUID tenantId;
    private UUID academicYearId;
    private UUID gradesAcademicYearId;
    private String diploma;
    private String educationLevel;
    private com.orientation.orientationapp.modules.orientation.entity.GradeScale gradeScale;
    private Map<String, com.orientation.orientationapp.modules.orientation.entity.GradeScale> subjectGradeScales;
    private String firstName;
    private String lastName;
    private String bacType;
    private String bacSeries;
    private String bacMention;
    private BigDecimal bacAverage;
    private Integer bacYear;
    private String country;
    private String nationality;
    private BigDecimal householdIncome;
    private String incomeCurrency;
    private String incomePeriod;
    private UUID targetProgramId;
    private UUID targetUniversityId;
    private String targetStudyField;
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
