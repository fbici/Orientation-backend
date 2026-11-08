package com.orientation.orientationapp.dataplat_import;
import com.orientation.orientationapp.dataplat_import.service.impl.ImportOrchestratorImpl;
import com.orientation.orientationapp.dataplat_history.repository.ImportHistoryRecordRepository;
import com.orientation.orientationapp.dataplat_history.entity.ImportHistoryRecord;
import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.enums.*;
import com.orientation.orientationapp.security.service.ResourceAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ImportOrchestratorTest {
 @Mock com.orientation.orientationapp.dataplat_parser.factory.ParserFactory parsers;
 @Mock com.orientation.orientationapp.modules.university.service.AcademicCatalogService catalogue;
 @Mock com.orientation.orientationapp.docintel.document.service.FactPayloadMapper payloads;
 @Mock com.fasterxml.jackson.databind.ObjectMapper json;
 @Mock jakarta.validation.Validator validator;
 @Mock ResourceAccess access;
 @Mock org.springframework.transaction.support.TransactionTemplate transactions;
 @Mock ImportHistoryRecordRepository history;
 @InjectMocks ImportOrchestratorImpl imports;
 @Test void missingFileBytesCannotBecomeSuccessfulImport() {
  var c=ImportContext.create(null,null,DataType.UNIVERSITIES,DataFormat.CSV);
  var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->imports.executeImport(c));
  assertEquals(422,error.getStatusCode().value());verifyNoInteractions(catalogue,history);
 }
 @Test void historyCannotBeReadAcrossTenants() {
  UUID id=UUID.randomUUID();var h=new ImportHistoryRecord();h.setId(id);h.setTenantId(UUID.randomUUID().toString());
  when(history.findById(id)).thenReturn(Optional.of(h));doThrow(new org.springframework.security.access.AccessDeniedException("Denied")).when(access).sameTenant(h.getTenantId());
  assertThrows(org.springframework.security.access.AccessDeniedException.class,()->imports.getImportStatus(id));
 }
 @Test void cancellationNeverPretendsToRollbackCommittedDrafts() {
  UUID id=UUID.randomUUID();var h=new ImportHistoryRecord();h.setId(id);h.setTenantId(UUID.randomUUID().toString());h.setStatus(ImportHistoryRecord.ImportStatus.COMPLETED);h.setTotalRows(1);h.setImportedRows(1);
  when(history.findById(id)).thenReturn(Optional.of(h));
  var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->imports.cancelImport(id));
  assertEquals(409,error.getStatusCode().value());verify(history,never()).save(any());verifyNoInteractions(catalogue);
 }
}
