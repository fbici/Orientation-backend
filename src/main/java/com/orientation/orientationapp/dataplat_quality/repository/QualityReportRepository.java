package com.orientation.orientationapp.dataplat_quality.repository;

import com.orientation.orientationapp.dataplat_quality.entity.QualityReportRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QualityReportRepository extends JpaRepository<QualityReportRecord, UUID> {
    Optional<QualityReportRecord> findByImportHistoryId(UUID importHistoryId);
}
