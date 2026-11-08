package com.orientation.orientationapp.dataplat_import.service.impl;
import com.orientation.orientationapp.dataplat_formats.core.model.*;
import com.orientation.orientationapp.dataplat_formats.enums.*;
import com.orientation.orientationapp.dataplat_import.service.ImportOrchestrator;
import com.orientation.orientationapp.dataplat_history.entity.ImportHistoryRecord;
import com.orientation.orientationapp.dataplat_history.repository.ImportHistoryRecordRepository;
import com.orientation.orientationapp.dataplat_parser.factory.ParserFactory;
import com.orientation.orientationapp.dataplat_formats.converter.RowMapper;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import com.orientation.orientationapp.docintel.document.service.FactPayloadMapper;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.intelligence.pipeline.PersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.time.*;
import java.io.*;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.*;
/** Existing import orchestrator: real bounded parsing and atomic draft writes, never official publication. */
@Service @RequiredArgsConstructor
public class ImportOrchestratorImpl implements ImportOrchestrator {
 private final ParserFactory parsers;private final AcademicCatalogService catalogue;
 private final FactPayloadMapper payloads;private final ObjectMapper json;private final Validator validator;
 private final ResourceAccess access;private final TransactionTemplate transactions;
 private final ImportHistoryRecordRepository history;
 private String kind(DataType type){return switch(type){case UNIVERSITIES->"UNIVERSITY";case FACULTIES->"FACULTY";case PROGRAMS->"PROGRAM";case SCHOLARSHIPS->"SCHOLARSHIP";default->throw invalid("This import type is unsupported; admission rules and guides require document review");};}
 @Override public ImportResult executeImport(ImportContext c){return execute(c,false);}
 @Override public ImportResult executeDryRun(ImportContext c){return execute(c,true);}
 private ImportResult execute(ImportContext c,boolean dry) {
  access.require("IMPORT_START");var actor=access.current();String k=kind(c.getDataType());
  Object raw=c.getProperties()==null?null:c.getProperties().get("fileBytes");
  if(!(raw instanceof byte[] bytes) || bytes.length==0 || bytes.length>2*1024*1024)throw invalid("Import file must contain 1 byte..2 MiB");
  if(!Set.of(DataFormat.CSV,DataFormat.EXCEL).contains(c.getSourceFormat()))throw invalid("Only CSV and XLSX tabular imports are supported");
  var result=ImportResult.builder().importId(c.getImportId()).startedAt(Instant.now()).build();
  var h=new ImportHistoryRecord();h.setFileName(c.getSourceIdentifier());h.setFileType(c.getSourceFormat().name());h.setFileSize((long)bytes.length);h.setChecksum(PersistenceService.digestBytes(bytes));
  h.setDataType(c.getDataType().name());h.setUserId(actor.getId().toString());h.setTenantId(actor.getTenant().getId().toString());h.setTotalRows(0);h.setExecutionTimeMs(0L);
  h.setSourceBytes(bytes);
  try {
   if(c.getSourceFormat()==DataFormat.CSV) {
    var decoder=java.nio.charset.StandardCharsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT);
    String text=decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();if(text.indexOf(0)>=0)throw invalid("CSV must be UTF-8 text without NUL bytes");
   }
   var parser=parsers.getParser(c.getSourceFormat());if(!parser.canParse(new ByteArrayInputStream(bytes)))throw invalid("File signature or tabular structure is invalid");
   var rows=parser.parse(new ByteArrayInputStream(bytes),c);
   if(rows.isEmpty() || rows.size()>1000)throw invalid("Import requires 1..1000 data rows");
   h.setTotalRows(rows.size());result.setTotalRecords(rows.size());
   Class<?> type=payloads.type(k);var requests=new ArrayList<Object>();int line=1;
   for(var row:rows) {
    line++;var values=new LinkedHashMap<String,Object>();
    for(var component:type.getRecordComponents()) {
     Object value=row.get(RowMapper.normalizeKey(component.getName()));
     if(value==null)value=row.get(component.getName());
     if(value==null)value=row.get(component.getName().replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(java.util.Locale.ROOT));
     if(value instanceof String s && s.isBlank())value=null;
     if(value!=null)values.put(component.getName(),value);
    }
    if(c.getCountryId()!=null)values.putIfAbsent("countryId",c.getCountryId());
    if(c.getAcademicYearId()!=null)values.putIfAbsent("academicYearId",c.getAcademicYearId());
    Object request;
    try{request=json.convertValue(values,type);}catch(IllegalArgumentException ex){throw invalid("Invalid field type at data row "+line);}
    var bad=validator.validate(request);if(!bad.isEmpty())throw invalid("Data row "+line+": "+bad.stream().map(v->v.getPropertyPath().toString()).sorted().distinct().toList());
    requests.add(request);
   }
   h.setRowsData(payloads.encode(rows));
   transactions.executeWithoutResult(tx-> {
    for(Object request:requests) {
     if(request instanceof com.orientation.orientationapp.modules.university.dto.CatalogRequests.UniversityWrite r)catalogue.saveUniversity(null,r);
     else if(request instanceof com.orientation.orientationapp.modules.university.dto.CatalogRequests.FacultyWrite r)catalogue.saveFaculty(null,r);
     else if(request instanceof com.orientation.orientationapp.modules.university.dto.CatalogRequests.ProgramWrite r)catalogue.saveProgram(null,r);
     else if(request instanceof com.orientation.orientationapp.modules.university.dto.CatalogRequests.ScholarshipWrite r)catalogue.saveScholarship(null,r);
    }
    if(dry){tx.setRollbackOnly();return;}
    h.setStatus(ImportHistoryRecord.ImportStatus.COMPLETED);h.setImportedRows(requests.size());h.setComments("TO_REVIEW: imported as drafts; source-guide review and publication required");
    h.setExecutionTimeMs(Duration.between(result.getStartedAt(),Instant.now()).toMillis());history.saveAndFlush(h);
   });
   result.setImportId(dry?c.getImportId():h.getId());result.setProcessedRecords(requests.size());result.setSuccessRecords(dry?0:requests.size());result.markCompleted();
   result.getWarnings().add(dry?"DRY_RUN: all writes rolled back; no history persisted":"TO_REVIEW: draft import only; no official publication, graph or search indexing");
  } catch(Exception ex) {
   result.markFailed(ex instanceof org.springframework.web.server.ResponseStatusException status?status.getReason():"Import rejected: invalid file or academic reference");
   result.setFailedRecords(Math.max(1,result.getTotalRecords()));h.setStatus(ImportHistoryRecord.ImportStatus.FAILED);h.setRejectedRows(result.getFailedRecords());h.setComments(String.join("; ",result.getErrors()));
   h.setExecutionTimeMs(Duration.between(result.getStartedAt(),Instant.now()).toMillis());
   if(!dry){transactions.executeWithoutResult(tx->history.saveAndFlush(h));result.setImportId(h.getId());}
  }
  return result;
 }
 @Override public void cancelImport(UUID id){getImportStatus(id);throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Completed atomic draft imports cannot be rolled back through cancellation; review or deactivate their draft entities");}
 @Override public ImportResult getImportStatus(UUID id){
  access.require("IMPORT_READ");var h=history.findById(id).orElseThrow(ResourceAccess::denied);access.sameTenant(h.getTenantId());
  return ImportResult.builder().importId(h.getId()).status(ImportStatus.valueOf(h.getStatus().name())).totalRecords(h.getTotalRows()).processedRecords(h.getImportedRows()).successRecords(h.getImportedRows()).failedRecords(h.getRejectedRows()).warnings(List.of(h.getComments()==null?"":h.getComments())).build();
 }
}
