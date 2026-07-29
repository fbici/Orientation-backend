package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.modules.orientation.entity.AdmissionCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdmissionCriterionRepository extends JpaRepository<AdmissionCriterion, UUID> {
    List<AdmissionCriterion> findByGuideVersionId(UUID guideVersionId);
    List<AdmissionCriterion> findByGuideVersionIdAndProgramId(UUID guideVersionId, UUID programId);
    List<AdmissionCriterion> findByGuideVersionIdAndFacultyId(UUID guideVersionId, UUID facultyId);
}
