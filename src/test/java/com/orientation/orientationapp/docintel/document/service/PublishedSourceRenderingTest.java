package com.orientation.orientationapp.docintel.document.service;
import com.orientation.orientationapp.storage.service.StorageService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.intelligence.pipeline.PersistenceService;
import org.apache.pdfbox.pdmodel.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PublishedSourceRenderingTest {
 @TempDir Path directory;
 @Test void originalPdfPageRendersButInvalidPageAndChangedSourceAreRejected() throws Exception {
  Path file=directory.resolve("synthetic.pdf");
  try(var pdf=new PDDocument()){pdf.addPage(new PDPage());pdf.save(file.toFile());}
  var storage=mock(StorageService.class);when(storage.load("synthetic.pdf")).thenReturn(file);
  var renderer=new DocumentPagePreview(mock(DocumentService.class),storage,mock(ResourceAccess.class));
  String checksum=PersistenceService.digestBytes(Files.readAllBytes(file));
  byte[] png=renderer.renderSource("synthetic.pdf",checksum,"application/pdf",1);
  assertEquals(137,Byte.toUnsignedInt(png[0]));assertEquals(80,png[1]);
  assertThrows(ResponseStatusException.class,()->renderer.renderSource("synthetic.pdf",checksum,"application/pdf",2));
  assertThrows(ResponseStatusException.class,()->renderer.renderSource("synthetic.pdf",checksum,"application/pdf",0));
  assertThrows(ResponseStatusException.class,()->renderer.renderSource("synthetic.pdf","changed","application/pdf",1));
 }
}
