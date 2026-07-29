package com.orientation.orientationapp.ai.comparator.service;

import com.orientation.orientationapp.ai.comparator.model.ComparisonResult;

import java.util.UUID;

public interface ComparisonService {

    /**
     * Compare two programs.
     *
     * @param programIdA first program ID
     * @param programIdB second program ID
     * @return the comparison result
     */
    ComparisonResult comparePrograms(UUID programIdA, UUID programIdB);

    /**
     * Compare two universities.
     *
     * @param universityIdA first university ID
     * @param universityIdB second university ID
     * @return the comparison result
     */
    ComparisonResult compareUniversities(UUID universityIdA, UUID universityIdB);
}
