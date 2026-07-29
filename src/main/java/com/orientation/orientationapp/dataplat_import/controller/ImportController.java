package com.orientation.orientationapp.dataplat_import.controller;

import com.orientation.orientationapp.dataplat_formats.core.model.*;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_formats.enums.ImportStatus;
import com.orientation.orientationapp.dataplat_import.dto.request.ImportRequest;
import com.orientation.orientationapp.dataplat_import.dto.response.ImportResponse;
import com.orientation.orientationapp.dataplat_import.service.ImportOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportOrchestrator importOrchestrator;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_START')")
    public ResponseEntity<ImportResponse> startImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dataType") DataType dataType,
            @RequestParam(value = "countryId", required = false) UUID countryId,
            @RequestParam(value = "academicYearId", required = false) UUID academicYearId) {

        try {
            UUID importId = UUID.randomUUID();
            DataFormat format = DataFormat.fromExtension(
                    file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1));

            ImportContext context = ImportContext.create(null, academicYearId, dataType, format);
            context.setCountryId(countryId);
            context.setSourceIdentifier(file.getOriginalFilename());
            context.setUploadedBy("current-user");

            ImportResult result = importOrchestrator.executeImport(context);

            return ResponseEntity.ok(ImportResponse.builder()
                    .importId(result.getImportId())
                    .status(result.getStatus())
                    .totalRecords(result.getTotalRecords())
                    .successRecords(result.getSuccessRecords())
                    .failedRecords(result.getFailedRecords())
                    .message(result.getStatus().getDescription())
                    .build());

        } catch (Exception e) {
            log.error("Import failed", e);
            return ResponseEntity.badRequest().body(ImportResponse.builder()
                    .message("Import failed: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ')")
    public ResponseEntity<ImportResult> getImportStatus(@PathVariable UUID id) {
        ImportResult result = importOrchestrator.getImportStatus(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_ROLLBACK')")
    public ResponseEntity<Map<String, String>> rollback(@PathVariable UUID id) {
        importOrchestrator.cancelImport(id);
        return ResponseEntity.ok(Map.of("message", "Rollback initiated for import " + id));
    }

    @GetMapping("/catalog")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ')")
    public ResponseEntity<Map<String, String>> getCatalog() {
        return ResponseEntity.ok(Map.of("message", "Import catalog"));
    }

    @GetMapping("/versions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ')")
    public ResponseEntity<Map<String, String>> getVersions() {
        return ResponseEntity.ok(Map.of("message", "Import versions"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ')")
    public ResponseEntity<Map<String, String>> getHistory() {
        return ResponseEntity.ok(Map.of("message", "Import history"));
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ')")
    public ResponseEntity<Map<String, String>> getStatistics(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("message", "Import statistics for " + id));
    }

    @GetMapping("/{id}/quality")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ')")
    public ResponseEntity<Map<String, String>> getQualityReport(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("message", "Quality report for " + id));
    }
}
