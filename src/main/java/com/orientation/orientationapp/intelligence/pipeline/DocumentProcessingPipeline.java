package com.orientation.orientationapp.intelligence.pipeline;
import com.orientation.orientationapp.intelligence.ocr.OcrService;
import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService;
import com.orientation.orientationapp.intelligence.classification.DocumentClassificationService;
import com.orientation.orientationapp.docintel.document.service.DocumentService;
import com.orientation.orientationapp.docintel.document.service.DocumentProcessingState;
import com.orientation.orientationapp.storage.service.StorageService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service @RequiredArgsConstructor
public class DocumentProcessingPipeline {
 private final OcrService ocr;
 private final EntityExtractionService extractor;
 private final DocumentClassificationService classifier;
 private final PersistenceService persistence;
 private final DocumentService documents;
 private final DocumentProcessingState state;
 private final StorageService storage;
 private final ResourceAccess access;
 private final ApplicationEventPublisher events;
 public PipelineResult processStoredDocument(UUID id) {
  access.require("DOCUMENT_MANAGE");
  var source=documents.getById(id);
  var result=PipelineResult.builder().documentId(id).fileName(source.getOriginalFileName()).startTime(System.currentTimeMillis()).build();
  if(!state.begin(id)) {
   result.setExtractedText(persistence.text(id));result.setDocumentType(source.getDocumentType().name());
   result.setStatus(source.getStatus());result.setEntities(extractor.extract(result.getExtractedText(),result.getDocumentType()));
   result.addStep("STORED","Existing source version and review facts retained");result.setEndTime(System.currentTimeMillis());return result;
  }
  try {
   progress(id,"OCR","Text extraction started");
   byte[] content=java.nio.file.Files.readAllBytes(storage.load(source.getStoredFileName()));
   if(!PersistenceService.digestBytes(content).equals(source.getChecksum()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Stored source checksum differs");
   var extracted=ocr.extract(content,source.getMimeType());result.setExtractedText(extracted.getRawText());
   result.addStep("OCR",extracted.getPageCount()+" page(s) extracted");
   String type=classifier.classify(extracted.getRawText(),source.getOriginalFileName());result.setDocumentType(type);
   progress(id,"EXTRACTION","Draft extraction started");
   int count=persistence.stage(id,extracted,type);
   result.setEntities(extractor.extract(extracted.getRawText(),type));result.addStep("DRAFTS",count+" source-backed draft fact(s)");
   result.setStatus("READY_REVIEW");result.addStep("REVIEW","Human validation required before publication");
   progress(id,"READY_REVIEW","Source and draft facts stored");
   result.setEndTime(System.currentTimeMillis());return result;
  } catch(Exception ex) {
   String message=ex instanceof ResponseStatusException status && status.getReason()!=null?status.getReason():"Document processing failed";
   state.failed(id,message);progress(id,"FAILED",message);
   if(ex instanceof ResponseStatusException status)throw status;
   throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,message);
  }
 }
 public PipelineResult processDocument(UUID id,byte[] ignored,String ignoredName) {
  return processStoredDocument(id); // Stored, authorized, versioned bytes are the only source of truth.
 }
 private void progress(UUID id,String step,String message) {
  events.publishEvent(new PipelineProgressEvent(id,step,message,access.current().getId()));
 }
 @lombok.Data @lombok.Builder
 public static class PipelineResult {
  private UUID documentId;private String fileName,status,error,extractedText,documentType;
  private EntityExtractionService.ExtractedEntities entities;
  @lombok.Builder.Default private List<String[]> steps=new ArrayList<>();
  private long startTime,endTime;
  public void addStep(String name,String detail){steps.add(new String[]{name,detail});}
 }
 public record PipelineProgressEvent(UUID documentId,String step,String message,UUID userId) {}
 public record PipelineCompletedEvent(UUID documentId,EntityExtractionService.ExtractedEntities entities) {}
}
