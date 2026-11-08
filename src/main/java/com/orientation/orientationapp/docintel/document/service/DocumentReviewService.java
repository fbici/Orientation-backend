package com.orientation.orientationapp.docintel.document.service;
import com.orientation.orientationapp.docintel.document.entity.*;
import com.orientation.orientationapp.docintel.document.repository.*;
import com.orientation.orientationapp.docintel.document.dto.request.*;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.intelligence.pipeline.PersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.time.Instant;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.row;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DocumentReviewService {
 private final DocumentRepository documents;
 private final DocumentVersionRepository versions;
 private final DocumentPageRepository pages;
 private final ExtractedFactRepository facts;
 private final DocumentAuditRepository audits;
 private final ResourceAccess access;
 private final FactPayloadMapper payloads;
 private Document checked(UUID id,boolean write) {
  Document d=(write?documents.lock(id):documents.findById(id)).orElseThrow(ResourceAccess::denied);
  access.require(write?"GUIDE_UPDATE":"DOCUMENT_READ");
  access.document(d.getTenantId(),d.getUploadedBy(),write);return d;
 }
 public Map<String,Object> factView(ExtractedFact f) {
  return row("id",f.getId(),"version",f.getVersion(),"documentId",f.getDocument().getId(),
   "documentVersionId",f.getDocumentVersion().getId(),"sourceVersion",f.getDocumentVersion().getVersionNumber(),
   "pageNumber",f.getPageNumber(),"passage",f.getSourcePassage(),"kind",f.getKind(),"originalKind",f.getOriginalKind(),
   "values",payloads.values(f),"originalValues",decodeOriginal(f),"status",f.getStatus(),"confidence",f.getConfidence(),
   "reviewedBy",f.getReviewedBy(),"reviewedAt",f.getReviewedAt(),"note",f.getReviewNote(),
   "publishedEntityId",f.getPublishedEntityId(),"publishedEntityType",f.getPublishedEntityType(),
   "publishedAt",f.getPublishedAt(),"guideVersionId",f.getGuideVersionId(),"academicYearId",f.getAcademicYearId());
 }
 private Object decodeOriginal(ExtractedFact f) {
  var copy=new ExtractedFact();copy.setPayload(f.getOriginalPayload());return payloads.values(copy);
 }
 public List<Map<String,Object>> facts(UUID id) {checked(id,false);return facts.findByDocumentIdOrderByPageNumberAscCreatedAtAsc(id).stream().map(this::factView).toList();}
 public List<Map<String,Object>> pages(UUID id) {
  checked(id,false);
  return pages.findByDocumentIdOrderByPageNumber(id).stream().map(p -> row("id",p.getId(),"pageNumber",p.getPageNumber(),
   "text",p.getRawText(),"ocrConfidence",p.getOcrConfidence(),"width",p.getWidth(),"height",p.getHeight())).toList();
 }
 public Map<String,Object> review(UUID id) {
  var d=checked(id,false);
  var source=versions.findByDocumentIdOrderByVersionNumberDesc(id).stream().findFirst();
  return row("documentId",id,"tenantId",d.getTenantId(),"title",d.getTitle(),"status",d.getStatus(),"mimeType",d.getMimeType(),"pageCount",d.getPageCount(),
   "processingError",d.getProcessingError(),"processedAt",d.getProcessedAt(),"checksum",d.getChecksum(),
   "documentVersionId",source.map(DocumentVersion::getId).orElse(null),"sourceVersion",source.map(DocumentVersion::getVersionNumber).orElse(null),
   "facts",facts(id),"pages",pages(id));
 }
 @Transactional public Map<String,Object> decide(UUID documentId,UUID factId,ReviewFactRequest request) {
  var d=checked(documentId,true);
  if(!Set.of("TO_REVIEW","VALIDATED","REJECTED").contains(request.status()))throw invalid("Invalid review status");
  var f=facts.lock(factId).orElseThrow(ResourceAccess::denied);
  if(!f.getDocument().getId().equals(documentId))throw ResourceAccess.denied();
  if(f.getPublishedAt()!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Published source facts are immutable; create a new guide version");
  if(!Objects.equals(f.getVersion(),request.version()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Fact changed since it was opened; reload before reviewing");
  String old=f.getPayload();
  if(request.kind()!=null) {if(!FactPayloadMapper.KINDS.contains(request.kind()))throw invalid("Unsupported fact kind");f.setKind(request.kind());}
  if(f.getKind().endsWith("CRITERION"))access.require("RULE_EDIT");
  if("VALIDATED".equals(request.status()))payloads.request(f,request.values(),false);
  f.setPayload(payloads.encodeFact(request.values()));f.setStatus(request.status());f.setReviewNote(request.note());
  f.setReviewedBy(access.current().getId());f.setReviewedAt(Instant.now());facts.saveAndFlush(f);
  audit(d,"REVIEW_FACT",payloads.encode(row("factId",f.getId(),"status",f.getStatus(),"previousValues",old,"values",request.values(),"note",request.note())));
  updateStatus(d);
  return factView(f);
 }
 @Transactional public Map<String,Object> add(UUID documentId,AddFactRequest request) {
  var d=checked(documentId,true);
  if(!FactPayloadMapper.KINDS.contains(request.kind()))throw invalid("Unsupported fact kind");
  var page=pages.findByDocumentIdOrderByPageNumber(documentId).stream().filter(p -> p.getPageNumber()==request.pageNumber()).findFirst()
   .orElseThrow(()->invalid("Source page is unavailable"));
  if(!page.getRawText().contains(request.passage()))throw invalid("Passage must appear verbatim on the selected source page");
  var source=versions.findByDocumentIdOrderByVersionNumberDesc(documentId).stream().findFirst().orElseThrow(()->invalid("Source version unavailable"));
  String key=PersistenceService.digest(request.kind()+"|"+request.pageNumber()+"|"+request.passage());
  if(facts.findByDocumentVersionIdAndSourceKey(source.getId(),key).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"This source fact already exists");
  var f=new ExtractedFact();f.setDocument(d);f.setDocumentVersion(source);f.setPageNumber(request.pageNumber());f.setSourcePassage(request.passage());
  f.setSourceKey(key);f.setKind(request.kind());f.setOriginalKind(request.kind());f.setPayload(payloads.encodeFact(request.values()));f.setOriginalPayload(f.getPayload());f.setStatus("TO_REVIEW");
  facts.saveAndFlush(f);audit(d,"ADD_SOURCE_FACT",payloads.encode(row("factId",f.getId(),"pageNumber",f.getPageNumber(),"kind",f.getKind())));
  updateStatus(d);return factView(f);
 }
 private void updateStatus(Document d) {
  var all=facts.findByDocumentIdOrderByPageNumberAscCreatedAtAsc(d.getId());
  boolean published=all.stream().anyMatch(f -> f.getPublishedAt()!=null);
  boolean complete=!all.isEmpty() && all.stream().allMatch(f->"REJECTED".equals(f.getStatus()) || f.getPublishedAt()!=null);
  boolean reviewed=!all.isEmpty() && all.stream().allMatch(f -> Set.of("VALIDATED","REJECTED").contains(f.getStatus()));
  d.setStatus(published?(complete?"PUBLISHED":"PARTIALLY_PUBLISHED"):(reviewed?"VALIDATED":"READY_REVIEW"));documents.save(d);
 }
 private void audit(Document d,String action,String details) {
  audits.save(DocumentAudit.builder().document(d).action(action).details(details).userId(access.current().getId().toString()).performedAt(Instant.now()).build());
 }
}
