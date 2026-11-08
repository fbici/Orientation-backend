package com.orientation.orientationapp.modules.university.repository;

import com.orientation.orientationapp.common.enums.ProgramType;
import com.orientation.orientationapp.modules.university.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Program> {
    @org.springframework.data.jpa.repository.Query("select p from Program p where p.available=true and p.verified=true and p.faculty.verified=true and p.faculty.active=true and p.faculty.campus.verified=true and p.faculty.campus.active=true and p.faculty.campus.university.verified=true and p.faculty.campus.university.active=true and (p.faculty.campus.university.tenantId is null or p.faculty.campus.university.tenantId=:tenant)")
    List<Program> findPublishedAvailable(@org.springframework.data.repository.query.Param("tenant") UUID tenant);
    List<Program> findByFacultyId(UUID facultyId);
    List<Program> findByType(ProgramType type);
    List<Program> findByAvailableTrue();
    Optional<Program> findByNameAndFacultyId(String name, UUID facultyId);
    boolean existsByNameAndFacultyId(String name, UUID facultyId);
}
