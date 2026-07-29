package com.orientation.orientationapp.recengine.profile.analyzer;

import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for analyzing candidate academic profiles.
 */
public interface ProfileAnalyzer {

    /**
     * Build a complete academic profile from candidate data.
     *
     * @param candidateId the candidate ID
     * @return the academic profile
     */
    AcademicProfile analyze(UUID candidateId);

    /**
     * Build a profile from provided data.
     *
     * @param bacType      the bac type
     * @param bacAverage   the bac average
     * @param subjectGrades the subject grades
     * @return the academic profile
     */
    AcademicProfile analyze(String bacType, BigDecimal bacAverage, Map<String, BigDecimal> subjectGrades);
}
