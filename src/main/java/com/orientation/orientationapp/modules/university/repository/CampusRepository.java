package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.modules.university.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampusRepository extends JpaRepository<Campus, UUID> {
    List<Campus> findByUniversityId(UUID universityId);
    List<Campus> findByCityId(UUID cityId);
    Optional<Campus> findByNameAndUniversityId(String name, UUID universityId);
    boolean existsByNameAndUniversityId(String name, UUID universityId);
}
