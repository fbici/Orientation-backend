package com.orientation.orientationapp.dataplat_import.controller;
import com.orientation.orientationapp.dataplat_formats.core.model.*;
import com.orientation.orientationapp.dataplat_formats.enums.*;
import com.orientation.orientationapp.dataplat_import.service.ImportOrchestrator;
import com.orientation.orientationapp.dataplat_history.repository.ImportHistoryRecordRepository;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.*;
@RestController @RequestMapping("/imports") @RequiredArgsConstructor
public class ImportController {
 private final ImportOrchestrator orchestrator;private final ImportHistoryRecordRepository history;private final ResourceAccess access;
 @PostMapping public ResponseEntity<ImportResult> start(@RequestParam MultipartFile file,@RequestParam DataType dataType,@RequestParam(required=false)UUID countryId,@RequestParam(required=false)UUID academicYearId,@RequestParam(defaultValue="false")boolean dryRun) throws java.io.IOException {
  access.require("IMPORT_START");
  String name=file.getOriginalFilename();if(name==null || name.length()>255 || name.codePoints().anyMatch(Character::isISOControl))throw invalid("Invalid import file name");
  String extension=name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);
  if(!Set.of("csv","xlsx").contains(extension))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Supported tabular imports: CSV and XLSX; PDF and images use document review");
  if(file.isEmpty() || file.getSize()>2*1024*1024)throw invalid("Import limit: 2 MiB");
  var c=ImportContext.create(null,academicYearId,dataType,DataFormat.fromExtension(extension));c.setCountryId(countryId);c.setSourceIdentifier(name);c.setUploadedBy(access.current().getId().toString());c.getProperties().put("fileBytes",file.getBytes());
  var result=dryRun?orchestrator.executeDryRun(c):orchestrator.executeImport(c);
  return ResponseEntity.status(result.getStatus()==ImportStatus.FAILED?422:200).body(result);
 }
 @GetMapping({"/{id}","/{id}/statistics","/{id}/quality"})public ImportResult get(@PathVariable UUID id){return orchestrator.getImportStatus(id);}
 @PostMapping("/{id}/rollback")public void rollback(@PathVariable UUID id){access.require("IMPORT_ROLLBACK");orchestrator.cancelImport(id);}
 @GetMapping({"","/history","/versions"}) @Transactional(readOnly=true)
 public Page<Map<String,Object>> list(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){
  access.require("IMPORT_READ");if(page<0 || size<1 || size>100)throw invalid("Invalid import pagination");
  var tenant=access.current().getTenant().getId().toString();boolean global=access.global();
  return history.findAll((r,q,cb)->global?cb.conjunction():cb.equal(r.get("tenantId"),tenant),PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt")))
   .map(h->row("id",h.getId(),"fileName",h.getFileName(),"fileType",h.getFileType(),"dataType",h.getDataType(),"checksum",h.getChecksum(),"totalRows",h.getTotalRows(),"importedRows",h.getImportedRows(),"rejectedRows",h.getRejectedRows(),"status",h.getStatus(),"publicationStatus","TO_REVIEW","comments",h.getComments(),"createdAt",h.getCreatedAt()));
 }
 @GetMapping("/catalog")public Map<String,Object> catalog(){access.require("IMPORT_READ");return row("formats",List.of("CSV","XLSX"),"dataTypes",List.of("UNIVERSITIES","FACULTIES","PROGRAMS","SCHOLARSHIPS"),"maxBytes",2097152,"maxRows",1000,"publicationStatus","TO_REVIEW","rollbackSupported",false,"documentWorkflow","/documents/upload");}
}
