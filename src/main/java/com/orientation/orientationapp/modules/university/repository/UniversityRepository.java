package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.common.enums.UniversityStatus;
import com.orientation.orientationapp.modules.university.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UniversityRepository extends JpaRepository<University, UUID> {
    List<University> findByCountryId(UUID countryId);
    List<University> findByCityId(UUID cityId);
    List<University> findByStatus(UniversityStatus status);
    List<University> findByActiveTrue();
    Optional<University> findByNameAndCountryId(String name, UUID countryId);
    boolean existsByNameAndCountryId(String name, UUID countryId);
}
