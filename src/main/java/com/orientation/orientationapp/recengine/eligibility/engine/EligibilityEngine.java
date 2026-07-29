package com.orientation.orientationapp.recengine.eligibility.engine;

import com.orientation.orientationapp.recengine.eligibility.model.EligibilityResult;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;

import java.util.Map;
import java.util.UUID;

/**
 * Interface for the Eligibility Engine.
 * Evaluates candidate eligibility against real admission criteria.
 */
public interface EligibilityEngine {

    /**
     * Evaluate eligibility for a specific program.
     *
     * @param profile     the academic profile
     * @param programId   the program ID
     * @param guideVersionId the guide version ID
     * @return the eligibility result
     */
    EligibilityResult evaluateProgram(AcademicProfile profile, UUID programId, UUID guideVersionId);

    /**
     * Evaluate eligibility for a specific scholarship.
     *
     * @param profile       the academic profile
     * @param scholarshipId the scholarship ID
     * @return the eligibility result
     */
    EligibilityResult evaluateScholarship(AcademicProfile profile, UUID scholarshipId);

    /**
     * Evaluate eligibility for a faculty.
     *
     * @param profile   the academic profile
     * @param facultyId the faculty ID
     * @return the eligibility result
     */
    EligibilityResult evaluateFaculty(AcademicProfile profile, UUID facultyId);
}
