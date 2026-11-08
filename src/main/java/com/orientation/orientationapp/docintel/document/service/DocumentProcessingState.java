package com.orientation.orientationapp.docintel.document.service;
import com.orientation.orientationapp.docintel.document.entity.Document;
import com.orientation.orientationapp.docintel.document.repository.DocumentRepository;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service @RequiredArgsConstructor
public class DocumentProcessingState {
 private final DocumentRepository documents;
 private final ResourceAccess access;
 private Document locked(UUID id) {
  var d=documents.lock(id).orElseThrow(ResourceAccess::denied);
  access.require("DOCUMENT_MANAGE");access.document(d.getTenantId(),d.getUploadedBy(),true);return d;
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW)
 public boolean begin(UUID id) {
  var d=locked(id);
  if(Set.of("READY_REVIEW","VALIDATED","PUBLISHED","PARTIALLY_PUBLISHED").contains(d.getStatus()))return false;
  if("PROCESSING".equals(d.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Document is already being processed");
  d.setStatus("PROCESSING");d.setProcessingError(null);documents.saveAndFlush(d);return true;
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW)
 public void failed(UUID id,String message) {
  var d=locked(id);if(!"PROCESSING".equals(d.getStatus()))return;
  d.setStatus("FAILED");d.setProcessingError(message);documents.saveAndFlush(d);
 }
}
