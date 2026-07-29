package com.orientation.orientationapp.modules.scholarship.repository;

import com.orientation.orientationapp.common.enums.ScholarshipStatus;
import com.orientation.orientationapp.common.enums.ScholarshipType;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, UUID> {
    List<Scholarship> findByCountryIdAndAcademicYearId(UUID countryId, UUID academicYearId);
    List<Scholarship> findByCountryId(UUID countryId);
    List<Scholarship> findByType(ScholarshipType type);
    List<Scholarship> findByStatus(ScholarshipStatus status);
    List<Scholarship> findByGovernmentTrue();
}
