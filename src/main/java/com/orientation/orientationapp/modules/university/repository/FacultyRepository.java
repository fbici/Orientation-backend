package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.modules.university.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    List<Faculty> findByCampusId(UUID campusId);
    Optional<Faculty> findByNameAndCampusId(String name, UUID campusId);
    boolean existsByNameAndCampusId(String name, UUID campusId);
}
