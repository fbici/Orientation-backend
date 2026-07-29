package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.modules.orientation.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    Optional<AcademicYear> findByLabel(String label);
    Optional<AcademicYear> findByCurrentTrue();
    boolean existsByLabel(String label);
}
