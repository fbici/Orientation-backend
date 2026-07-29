package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.common.enums.ImportStatus;
import com.orientation.orientationapp.modules.orientation.entity.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, UUID> {
    List<ImportHistory> findByGuideVersionId(UUID guideVersionId);
    List<ImportHistory> findByImportType(String importType);
    List<ImportHistory> findByStatus(ImportStatus status);
}
