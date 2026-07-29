package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.modules.orientation.entity.GradeScale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradeScaleRepository extends JpaRepository<GradeScale, UUID> {
    Optional<GradeScale> findByCountryIdAndAcademicYearId(UUID countryId, UUID academicYearId);
    List<GradeScale> findByCountryId(UUID countryId);
}
