package com.orientation.orientationapp.docintel.ocr.engine.impl;
import com.orientation.orientationapp.docintel.ocr.engine.OcrEngine;
import com.orientation.orientationapp.docintel.ocr.model.OcrResult;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
@Component
public class PdfImageOcrEngine implements OcrEngine {
 private final TesseractProcess tesseract;
 private final int maxPages,dpi;
 private final long maxPixels;
 public PdfImageOcrEngine(TesseractProcess tesseract,
  @Value("${app.ocr.max-pages:200}") int maxPages,@Value("${app.ocr.render-dpi:200}") int dpi,
  @Value("${app.ocr.max-page-pixels:16000000}") long maxPixels) {
  this.tesseract=tesseract;this.maxPages=Math.max(1,Math.min(maxPages,300));
  this.dpi=Math.max(100,Math.min(dpi,300));this.maxPixels=Math.max(1_000_000,Math.min(maxPixels,40_000_000));
 }
 @Override public String getEngineId(){return "PDFBOX_TESSERACT";}
 @Override public String getEngineName(){return "PDFBox direct text and local Tesseract";}
 @Override public boolean supportsMimeType(String mime){return Set.of("application/pdf","image/png","image/jpeg").contains(mime);}
 @Override public double getConfidenceThreshold(){return 0.7;}
 private ResponseStatusException invalid(String message){return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,message);}
 @Override public OcrResult performOcr(InputStream input,String mime) {
  long start=System.currentTimeMillis();
  if(!supportsMimeType(mime))throw invalid("Unsupported OCR media type");
  try {
   byte[] bytes=input.readNBytes(10*1024*1024+1);
   if(bytes.length==0 || bytes.length>10*1024*1024)throw invalid("Document is empty or too large");
   var pages=new ArrayList<OcrResult.OcrPage>();boolean usedOcr=false;
   if(mime.equals("application/pdf")) {
    if(bytes.length<5 || !new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))throw invalid("Invalid PDF signature");
    try(var document=Loader.loadPDF(bytes)) {
     if(document.isEncrypted() || document.getNumberOfPages()==0 || document.getNumberOfPages()>maxPages)throw invalid("Encrypted, empty or excessive-page PDF");
     var stripper=new PDFTextStripper();stripper.setSortByPosition(true);
     var renderer=new PDFRenderer(document);
     for(int i=0;i<document.getNumberOfPages();i++) {
      stripper.setStartPage(i+1);stripper.setEndPage(i+1);
      String text=stripper.getText(document);
      var page=document.getPage(i);double width=page.getCropBox().getWidth()*dpi/72.0,height=page.getCropBox().getHeight()*dpi/72.0;
      pixels(width,height);
      double confidence=1.0; // Direct character extraction, not a measured OCR accuracy.
      if(text.isBlank() || text.strip().length()<80 && hasRaster(page.getResources(),0)) {
       BufferedImage image=renderer.renderImageWithDPI(i,dpi,ImageType.RGB);
       var recognized=tesseract.recognize(image);image.flush();
       text=recognized.text();confidence=recognized.confidence();usedOcr=true;
      }
      pages.add(OcrResult.OcrPage.builder().pageNumber(i+1).text(text).confidence(confidence).width((int)width).height((int)height).blocks(List.of()).build());
     }
    }
   } else {
    try(var imageInput=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
     var readers=ImageIO.getImageReaders(imageInput);
     if(!readers.hasNext())throw invalid("Invalid or unsupported image");
     var reader=readers.next();
     try {
      reader.setInput(imageInput,true,true);int width=reader.getWidth(0),height=reader.getHeight(0);pixels(width,height);
      var image=reader.read(0);var recognized=tesseract.recognize(image);image.flush();usedOcr=true;
      pages.add(OcrResult.OcrPage.builder().pageNumber(1).text(recognized.text()).confidence(recognized.confidence()).width(width).height(height).blocks(List.of()).build());
     } finally {reader.dispose();}
    }
   }
   String text=String.join("\n",pages.stream().map(OcrResult.OcrPage::getText).toList());
   if(text.isBlank())throw invalid("No readable text was extracted");
   if(text.length()>5_000_000)throw invalid("Extracted text exceeds its limit");
   return OcrResult.builder().rawText(text).cleanedText(text.strip()).pages(pages).pageCount(pages.size())
    .confidence(pages.stream().mapToDouble(OcrResult.OcrPage::getConfidence).average().orElse(0))
    .engineUsed(usedOcr?"PDFBOX_TESSERACT":"PDFBOX_DIRECT").processingTimeMs(System.currentTimeMillis()-start)
    .blocks(List.of()).paragraphs(Arrays.asList(text.split("\n\\s*\n"))).titles(List.of()).tables(List.of()).images(List.of()).build();
  } catch(ResponseStatusException ex){throw ex;}
  catch(IOException|IllegalArgumentException ex){throw invalid("Document cannot be decoded");}
 }
 private boolean hasRaster(org.apache.pdfbox.pdmodel.PDResources resources,int depth) throws IOException {
  if(resources==null || depth>5)return false;
  for(var name:resources.getXObjectNames()) {
   var object=resources.getXObject(name);
   if(object instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject)return true;
   if(object instanceof org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject form && hasRaster(form.getResources(),depth+1))return true;
  }return false;
 }
 private void pixels(double width,double height) {
  if(!Double.isFinite(width) || !Double.isFinite(height) || width<1 || height<1 || width*height>maxPixels)
   throw invalid("Page dimensions exceed the configured safe limit");
 }
}
