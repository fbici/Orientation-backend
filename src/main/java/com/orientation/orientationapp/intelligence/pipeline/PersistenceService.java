package com.orientation.orientationapp.intelligence.pipeline;
import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService;
import com.orientation.orientationapp.docintel.ocr.model.OcrResult;
import com.orientation.orientationapp.docintel.document.entity.*;
import com.orientation.orientationapp.docintel.document.repository.*;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.math.BigDecimal;
import java.time.Instant;
@Service @RequiredArgsConstructor
public class PersistenceService {
 private final DocumentRepository documents;
 private final DocumentVersionRepository versions;
 private final DocumentPageRepository pages;
 private final DocumentExtractionRepository extractions;
 private final ExtractedFactRepository facts;
 private final DocumentAuditRepository audits;
 private final EntityExtractionService extractor;
 private final ResourceAccess access;
 private final ObjectMapper json;
 public static String digestBytes(byte[] value) {
  try {return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value));}
  catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
 }
 public static String digest(String value) {
  try {return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
  catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
 }
 private Document checked(UUID id) {
  var d=documents.findById(id).orElseThrow(ResourceAccess::denied);
  access.document(d.getTenantId(),d.getUploadedBy(),false);return d;
 }
 @Transactional(readOnly=true) public String text(UUID id) {
  checked(id);return extractions.findByDocumentId(id).map(DocumentExtraction::getFullText).orElse("");
 }
 @Transactional
 public int stage(UUID id,OcrResult result,String type) {
  var d=documents.lock(id).orElseThrow(ResourceAccess::denied);
  access.require("DOCUMENT_MANAGE");access.document(d.getTenantId(),d.getUploadedBy(),true);
  if(!"PROCESSING".equals(d.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Document is not in processing state");
  var sources=versions.findByDocumentIdOrderByVersionNumberDesc(id);
  if(sources.isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Stored source version is missing");
  var source=sources.getFirst();
  int factCount=0;
  for(var page:result.getPages()) {factCount+=extractor.extractPage(page.getText(),page.getPageNumber()).size();if(factCount>5000)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Document exceeds 5000 review facts");}
  var existingPages=new HashMap<Integer,DocumentPage>();
  pages.findByDocumentIdOrderByPageNumber(id).forEach(p -> existingPages.put(p.getPageNumber(),p));
  for(var page:result.getPages()) {
   var stored=existingPages.getOrDefault(page.getPageNumber(),new DocumentPage());
   stored.setDocument(d);stored.setPageNumber(page.getPageNumber());stored.setRawText(page.getText());stored.setCleanedText(page.getText().strip());
   stored.setOcrConfidence(BigDecimal.valueOf(page.getConfidence()));stored.setWidth(page.getWidth());stored.setHeight(page.getHeight());pages.save(stored);
   for(var draft:extractor.extractPage(page.getText(),page.getPageNumber())) {
    String key=digest(draft.kind()+"|"+draft.pageNumber()+"|"+draft.passage());
    if(facts.findByDocumentVersionIdAndSourceKey(source.getId(),key).isPresent())continue;
    var fact=new ExtractedFact();fact.setDocument(d);fact.setDocumentVersion(source);fact.setKind(draft.kind());fact.setOriginalKind(draft.kind());
    fact.setPageNumber(draft.pageNumber());fact.setSourceKey(key);fact.setSourcePassage(draft.passage());fact.setStatus("TO_REVIEW");
    try {String payload=json.writeValueAsString(draft.values());fact.setPayload(payload);fact.setOriginalPayload(payload);}
    catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException("Draft cannot be serialized");}
    facts.save(fact);
   }
  }
  var extraction=extractions.findByDocumentId(id).orElseGet(DocumentExtraction::new);
  extraction.setDocument(d);extraction.setFullText(result.getRawText());extraction.setCleanedText(result.getCleanedText());
  extraction.setOcrConfidence(BigDecimal.valueOf(result.getConfidence()));extraction.setOcrEngine(result.getEngineUsed());
  extraction.setBlockCount(result.getBlocks().size());extraction.setParagraphCount(result.getParagraphs().size());
  extraction.setTableCount(result.getTables().size());extraction.setImageCount(result.getImages().size());
  extractions.save(extraction);
  try {d.setDocumentType(Document.DocumentType.valueOf(type));}catch(IllegalArgumentException ex){d.setDocumentType(Document.DocumentType.UNKNOWN);}
  d.setPageCount(result.getPageCount());d.setOcrScore(BigDecimal.valueOf(result.getConfidence()));
  d.setQualityScore(null); // Technical extraction confidence is not a measured academic-data quality score.
  d.setProcessedAt(Instant.now());d.setProcessingError(null);d.setStatus("READY_REVIEW");documents.saveAndFlush(d);
  audits.save(DocumentAudit.builder().document(d).action("EXTRACT_TO_DRAFT").performedAt(Instant.now())
   .userId(access.current().getId().toString()).details("Page text and review facts stored; no business publication").build());
  return facts.findByDocumentIdOrderByPageNumberAscCreatedAtAsc(id).size();
 }
 @Deprecated public void persist(EntityExtractionService.ExtractedEntities entities,UUID id) {
  throw new ResponseStatusException(HttpStatus.CONFLICT,"Automatic business publication is forbidden; validate source facts first");
 }
}
