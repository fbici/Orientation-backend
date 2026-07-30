package com.orientation.orientationapp.intelligence.ocr.impl;

import com.orientation.orientationapp.intelligence.ocr.OcrEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implémentation Tesseract de l'OCR Engine.
 *
 * Utilise Tesseract via tess4j pour l'extraction de texte
 * à partir d'images et de PDFs scannés.
 *
 * En mode développement, retourne des données simulées.
 * En production, nécessite Tesseract installé sur le serveur.
 */
@Slf4j
@Component
public class TesseractOcrEngine implements OcrEngine {

    @Override
    public String extractText(byte[] imageContent) {
        log.info("Tesseract OCR: processing image ({} bytes)", imageContent.length);

        // En production, utiliser tess4j :
        // Tesseract tesseract = new Tesseract();
        // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        // tesseract.setLanguage("fra+eng+ara");
        // return tesseract.doImage(imageContent);

        // Mode développement : extraction basique
        return "[Image OCR: " + imageContent.length + " bytes processed]";
    }

    @Override
    public String extractTextFromPdf(byte[] pdfContent) {
        log.info("Tesseract OCR: processing PDF ({} bytes)", pdfContent.length);

        // En production, convertir PDF en images puis OCR
        // PDFRenderer renderer = new PDFRenderer(document);
        // BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300);
        // return extractText(imageToBytes(image));

        return "[PDF OCR: " + pdfContent.length + " bytes processed]";
    }
}
