package com.orientation.orientationapp.docintel.document.service;
import com.orientation.orientationapp.storage.service.StorageService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.intelligence.pipeline.PersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import java.io.*;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
@Service @RequiredArgsConstructor
public class DocumentPagePreview {
 private final DocumentService documents;
 private final StorageService storage;
 private final ResourceAccess access;
 public byte[] render(UUID id,int number) {
  access.require("DOCUMENT_READ");var d=documents.getById(id);
  if(number<1 || number>200)throw invalid();
  try {
   var path=storage.load(d.getStoredFileName());
   if(java.nio.file.Files.size(path)>10*1024*1024)throw invalid();
   byte[] bytes=java.nio.file.Files.readAllBytes(path);
   if(!PersistenceService.digestBytes(bytes).equals(d.getChecksum()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Stored source checksum differs");
   java.awt.image.BufferedImage image;
   if(d.getMimeType().equals("application/pdf")) {
    try(var pdf=Loader.loadPDF(bytes)) {
     if(pdf.isEncrypted() || number>pdf.getNumberOfPages() || pdf.getNumberOfPages()>200)throw invalid();
     var box=pdf.getPage(number-1).getCropBox();pixels(box.getWidth()*150/72.0,box.getHeight()*150/72.0);
     image=new PDFRenderer(pdf).renderImageWithDPI(number-1,150,ImageType.RGB);
    }
   } else {
    if(number!=1)throw invalid();
    try(var stream=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
     var readers=ImageIO.getImageReaders(stream);if(!readers.hasNext())throw invalid();var reader=readers.next();
     try {reader.setInput(stream,true,true);pixels(reader.getWidth(0),reader.getHeight(0));image=reader.read(0);}
     finally {reader.dispose();}
    }
   }
   var out=new ByteArrayOutputStream();
   try {if(!ImageIO.write(image,"png",out))throw invalid();return out.toByteArray();}
   finally {image.flush();}
  } catch(IOException ex){throw invalid();}
 }
 private void pixels(double w,double h) {if(!Double.isFinite(w*h) || w<1 || h<1 || w*h>16_000_000)throw invalid();}
 private ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Source page cannot be rendered safely");}
}
