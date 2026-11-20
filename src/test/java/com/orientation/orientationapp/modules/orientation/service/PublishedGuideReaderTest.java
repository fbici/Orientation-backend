package com.orientation.orientationapp.modules.orientation.service;
import com.orientation.orientationapp.modules.orientation.repository.GuideVersionRepository;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.docintel.document.repository.DocumentVersionRepository;
import com.orientation.orientationapp.docintel.document.service.DocumentPagePreview;
import com.orientation.orientationapp.docintel.document.entity.*;
import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PublishedGuideReaderTest {
 final GuideCatalogueService catalogue=mock(GuideCatalogueService.class);
 final GuideVersionRepository versions=mock(GuideVersionRepository.class);
 final DocumentVersionRepository sources=mock(DocumentVersionRepository.class);
 final DocumentPagePreview preview=mock(DocumentPagePreview.class);
 final PublishedGuideReader reader=new PublishedGuideReader(catalogue,versions,sources,preview);
 final UUID guideId=UUID.randomUUID(),versionId=UUID.randomUUID(),sourceId=UUID.randomUUID();
 GuideVersion version() {
  var guide=new OrientationGuide();guide.setId(guideId);
  var v=new GuideVersion();v.setOrientationGuide(guide);v.setStatus(GuideVersionStatus.ACTIVE);v.setDocumentVersionId(sourceId);
  when(versions.findById(versionId)).thenReturn(Optional.of(v));return v;
 }
 @Test void deniedCatalogueScopeNeverReadsSource() {
  when(catalogue.guide(guideId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
  assertThrows(ResponseStatusException.class,()->reader.page(guideId,versionId,1));
  verifyNoInteractions(versions,sources,preview);
 }
 @Test void wrongGuideAndArchivedVersionsNeverReadSource() {
  var v=version();v.getOrientationGuide().setId(UUID.randomUUID());
  assertThrows(ResponseStatusException.class,()->reader.page(guideId,versionId,1));
  v.getOrientationGuide().setId(guideId);v.setStatus(GuideVersionStatus.ARCHIVED);
  assertThrows(ResponseStatusException.class,()->reader.page(guideId,versionId,1));
  v.setStatus(GuideVersionStatus.ACTIVE);v.setDocumentVersionId(null);
  assertThrows(ResponseStatusException.class,()->reader.page(guideId,versionId,1));
  verifyNoInteractions(sources,preview);
 }
 @Test void publishedVersionUsesItsOwnImmutableFileAndChecksum() {
  version();var document=new Document();document.setMimeType("application/pdf");document.setStoredFileName("new-private-version.pdf");
  var source=new DocumentVersion();source.setDocument(document);source.setStoredFileName("published.pdf");source.setChecksum("checksum");
  when(sources.findById(sourceId)).thenReturn(Optional.of(source));
  byte[] png={1,2};when(preview.renderSource("published.pdf","checksum","application/pdf",2)).thenReturn(png);
  assertArrayEquals(png,reader.page(guideId,versionId,2));
  document.setDeleted(true);
  assertThrows(ResponseStatusException.class,()->reader.page(guideId,versionId,2));
  verify(preview,times(1)).renderSource("published.pdf","checksum","application/pdf",2);
 }
}
