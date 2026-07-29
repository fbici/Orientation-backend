package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.modules.orientation.entity.OrientationGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrientationGuideRepository extends JpaRepository<OrientationGuide, UUID> {
    Optional<OrientationGuide> findByCountryIdAndAcademicYearId(UUID countryId, UUID academicYearId);
    List<OrientationGuide> findByCountryId(UUID countryId);
}
