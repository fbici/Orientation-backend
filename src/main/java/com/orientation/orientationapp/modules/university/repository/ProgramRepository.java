package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.common.enums.ProgramType;
import com.orientation.orientationapp.modules.university.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID> {
    List<Program> findByFacultyId(UUID facultyId);
    List<Program> findByType(ProgramType type);
    List<Program> findByAvailableTrue();
    Optional<Program> findByNameAndFacultyId(String name, UUID facultyId);
    boolean existsByNameAndFacultyId(String name, UUID facultyId);
}
