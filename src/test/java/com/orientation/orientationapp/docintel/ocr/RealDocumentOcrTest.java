package com.orientation.orientationapp.docintel.ocr;
import com.orientation.orientationapp.docintel.ocr.engine.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

class RealDocumentOcrTest {
 private PdfImageOcrEngine engine() {
  return new PdfImageOcrEngine(new TesseractProcess(System.getProperty("f2.tesseract","tesseract"),
   "fra+eng",System.getProperty("f2.tessdata",""),45),200,200,16_000_000);
 }
 static BufferedImage textImage(String content) {
  var image=new BufferedImage(1800,900,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();
  g.setColor(Color.WHITE);g.fillRect(0,0,image.getWidth(),image.getHeight());g.setColor(Color.BLACK);
  g.setFont(new Font("Arial",Font.PLAIN,48));int y=110;
  for(String line:content.split("\n")){g.drawString(line,70,y);y+=90;}g.dispose();return image;
 }
 static byte[] pdf(String direct,BufferedImage scanned) throws IOException {
  try(var document=new PDDocument();var out=new ByteArrayOutputStream()) {
   if(direct!=null) {
    var page=new PDPage(PDRectangle.A4);document.addPage(page);
    try(var stream=new PDPageContentStream(document,page)) {
     stream.beginText();stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA),16);
     stream.newLineAtOffset(50,750);stream.setLeading(24);
     for(String line:direct.split("\n")){stream.showText(line);stream.newLine();}
     stream.endText();
    }
   }
   if(scanned!=null) {
    var page=new PDPage(new PDRectangle(scanned.getWidth()*72f/200,scanned.getHeight()*72f/200));document.addPage(page);
    try(var stream=new PDPageContentStream(document,page)) {
     stream.drawImage(LosslessFactory.createFromImage(document,scanned),0,0,page.getMediaBox().getWidth(),page.getMediaBox().getHeight());
    }
   }
   document.save(out);return out.toByteArray();
  }
 }
 @Test void extractsCompressedPdfTextWithoutAnOcrExecutable() throws Exception {
  var result=engine().performOcr(new ByteArrayInputStream(pdf("University: Synthetic Test Academy\nProgramme: Licence Informatique",null)),"application/pdf");
  assertTrue(result.getRawText().contains("Synthetic Test Academy"));assertTrue(result.getRawText().contains("Licence Informatique"));
  assertEquals("PDFBOX_DIRECT",result.getEngineUsed());assertEquals(1,result.getPages().getFirst().getPageNumber());
 }
 @Test @EnabledIfSystemProperty(named="f2.tesseract",matches=".+")
 void recognizesActualPngAndJpegText() throws Exception {
  var image=textImage("University: Synthetic Academy\nProgramme: Licence Informatique\nDuration: 3 years");
  for(String type:new String[]{"png","jpeg"}) {
   var out=new ByteArrayOutputStream();assertTrue(ImageIO.write(image,type,out));
   var result=engine().performOcr(new ByteArrayInputStream(out.toByteArray()),"image/"+type);
   assertTrue(result.getRawText().toLowerCase().contains("synthetic academy"),result.getRawText());
   assertTrue(result.getRawText().toLowerCase().contains("informatique"),result.getRawText());
   assertTrue(result.getConfidence()>0 && result.getConfidence()<=1);
  }
 }
 @Test @EnabledIfSystemProperty(named="f2.tesseract",matches=".+")
 void recognizesScannedPdfAndPreservesMixedDocumentPageNumbers() throws Exception {
  var image=textImage("Programme: Licence Mathematiques\nSynthetic scanned page");
  var scanned=engine().performOcr(new ByteArrayInputStream(pdf(null,image)),"application/pdf");
  assertTrue(scanned.getRawText().toLowerCase().contains("mathematiques"),scanned.getRawText());
  var mixed=engine().performOcr(new ByteArrayInputStream(pdf("University: Text First Page",image)),"application/pdf");
  assertEquals(2,mixed.getPageCount());assertTrue(mixed.getPages().get(0).getText().contains("Text First Page"));
  assertEquals(2,mixed.getPages().get(1).getPageNumber());assertTrue(mixed.getPages().get(1).getText().toLowerCase().contains("mathematiques"));
 }
 @Test void refusesMalformedPdfAndNeverReturnsBinaryGarbageAsText() {
  var error=assertThrows(ResponseStatusException.class,()->engine().performOcr(new ByteArrayInputStream("%PDF-invalid".getBytes()),"application/pdf"));
  assertEquals(422,error.getStatusCode().value());
 }
 @Test void missingOcrExecutableProducesExplicit503() throws Exception {
  var image=textImage("Synthetic test");var out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);
  var missing=new PdfImageOcrEngine(new TesseractProcess("orientia-nonexistent-test-ocr","eng","",1),200,200,16_000_000);
  var error=assertThrows(ResponseStatusException.class,()->missing.performOcr(new ByteArrayInputStream(out.toByteArray()),"image/png"));
  assertEquals(503,error.getStatusCode().value());
 }

 @Test @EnabledIfSystemProperty(named="f2.tesseract",matches=".+")
 void imageBodyWithEmbeddedHeaderStillReceivesRealOcr() throws Exception {
  byte[] bytes;
  try(var d=new PDDocument();var out=new ByteArrayOutputStream()){
   var image=textImage("Programme: Licence Mathematiques\nSynthetic scanned body");var page=new PDPage(new PDRectangle(648,400));d.addPage(page);
   try(var stream=new PDPageContentStream(d,page)){
    stream.beginText();stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA),12);stream.newLineAtOffset(30,375);stream.showText("Synthetic guide header");stream.endText();
    stream.drawImage(LosslessFactory.createFromImage(d,image),0,0,648,324);
   }d.save(out);bytes=out.toByteArray();
  }
  var result=engine().performOcr(new ByteArrayInputStream(bytes),"application/pdf");
  assertTrue(result.getRawText().toLowerCase().contains("mathematiques"),result.getRawText());assertEquals("PDFBOX_TESSERACT",result.getEngineUsed());
 }
}
