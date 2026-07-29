package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.modules.orientation.entity.GradeScaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeScaleItemRepository extends JpaRepository<GradeScaleItem, UUID> {
    List<GradeScaleItem> findByGradeScaleIdOrderBySortOrder(UUID gradeScaleId);
}
