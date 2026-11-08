package com.orientation.orientationapp.intelligence.ocr;
import com.orientation.orientationapp.docintel.ocr.engine.impl.PdfImageOcrEngine;
import com.orientation.orientationapp.docintel.ocr.engine.impl.LocalOcrEngine;
import com.orientation.orientationapp.docintel.ocr.model.OcrResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.util.Locale;
@Service @RequiredArgsConstructor
public class OcrService {
 private final PdfImageOcrEngine documents;
 private final LocalOcrEngine text;
 public OcrResult extract(byte[] content,String mime) {
  if(!documents.supportsMimeType(mime) && !text.supportsMimeType(mime))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Unsupported OCR media type");
  return documents.supportsMimeType(mime)?documents.performOcr(new ByteArrayInputStream(content),mime)
   :text.performOcr(new ByteArrayInputStream(content),mime);
 }
 public String extractText(byte[] content,String fileName) {
  String extension=fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);
  String mime=switch(extension){case "pdf" -> "application/pdf";case "png" -> "image/png";case "jpg","jpeg" -> "image/jpeg";case "txt" -> "text/plain";default -> throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Unsupported OCR file type");};
  return extract(content,mime).getRawText();
 }
}
