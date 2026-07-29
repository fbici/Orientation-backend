package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.modules.orientation.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByGradeScaleId(UUID gradeScaleId);
    List<Subject> findByCoreTrue();
    Optional<Subject> findByNameAndGradeScaleId(String name, UUID gradeScaleId);
}
