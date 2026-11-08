package com.orientation.orientationapp.intelligence.ocr.impl;
import com.orientation.orientationapp.intelligence.ocr.OcrEngine;
import com.orientation.orientationapp.intelligence.ocr.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class TesseractOcrEngine implements OcrEngine {
 private final OcrService ocr;
 public String extractText(byte[] image){return ocr.extract(image,"image/png").getRawText();}
 public String extractTextFromPdf(byte[] pdf){return ocr.extract(pdf,"application/pdf").getRawText();}
}
