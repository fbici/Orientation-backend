package com.orientation.orientationapp.modules.scholarship.repository;

import com.orientation.orientationapp.modules.scholarship.entity.ScholarshipCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScholarshipCriterionRepository extends JpaRepository<ScholarshipCriterion, UUID> {
    List<ScholarshipCriterion> findByGuideVersionId(UUID guideVersionId);
    List<ScholarshipCriterion> findByScholarshipId(UUID scholarshipId);
    List<ScholarshipCriterion> findByGuideVersionIdAndScholarshipId(UUID guideVersionId, UUID scholarshipId);
}
