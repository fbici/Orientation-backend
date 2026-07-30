package com.orientation.orientationapp.intelligence.ocr;

import org.springframework.stereotype.Component;

/**
 * Interface pour les moteurs OCR.
 * Permet de supporter différents moteurs (Tesseract, Google Vision, etc.)
 */
@Component
public interface OcrEngine {

    /**
     * Extrait le texte d'une image.
     *
     * @param imageContent contenu binaire de l'image
     * @return texte extrait
     */
    String extractText(byte[] imageContent);

    /**
     * Extrait le texte d'un PDF scanné.
     *
     * @param pdfContent contenu binaire du PDF
     * @return texte extrait
     */
    String extractTextFromPdf(byte[] pdfContent);
}
