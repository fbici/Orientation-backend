package com.orientation.orientationapp.modules.orientation.service;
import com.orientation.orientationapp.modules.orientation.repository.GuideVersionRepository;
import com.orientation.orientationapp.docintel.document.repository.DocumentVersionRepository;
import com.orientation.orientationapp.docintel.document.service.DocumentPagePreview;
import com.orientation.orientationapp.docintel.document.entity.DocumentVersion;
import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.UUID;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class PublishedGuideReader {
 private final GuideCatalogueService catalogue;
 private final GuideVersionRepository versions;
 private final DocumentVersionRepository sources;
 private final DocumentPagePreview preview;
 public byte[] page(UUID guideId,UUID versionId,int number) {
  catalogue.guide(guideId); // Same tenant/publication scope as the candidate catalogue.
  var version=versions.findById(versionId).orElseThrow(PublishedGuideReader::unavailable);
  if(!version.getOrientationGuide().getId().equals(guideId) || version.getStatus()!=GuideVersionStatus.ACTIVE || version.getDocumentVersionId()==null)
   throw unavailable();
  DocumentVersion source=sources.findById(version.getDocumentVersionId()).orElseThrow(PublishedGuideReader::unavailable);
  if(Boolean.TRUE.equals(source.getDocument().getDeleted()))throw unavailable();
  return preview.renderSource(source.getStoredFileName(),source.getChecksum(),source.getDocument().getMimeType(),number);
 }
 private static ResponseStatusException unavailable(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Published guide source unavailable");}
}
