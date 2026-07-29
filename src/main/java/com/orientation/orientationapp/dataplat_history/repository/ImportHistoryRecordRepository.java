package com.orientation.orientationapp.dataplat_history.repository;

import com.orientation.orientationapp.dataplat_history.entity.ImportHistoryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportHistoryRecordRepository extends JpaRepository<ImportHistoryRecord, UUID> {
    Page<ImportHistoryRecord> findByDataType(String dataType, Pageable pageable);
    Page<ImportHistoryRecord> findByStatus(ImportHistoryRecord.ImportStatus status, Pageable pageable);
    Page<ImportHistoryRecord> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
