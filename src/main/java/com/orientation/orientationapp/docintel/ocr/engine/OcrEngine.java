package com.orientation.orientationapp.docintel.ocr.engine;

import com.orientation.orientationapp.docintel.ocr.model.OcrResult;

import java.io.InputStream;

/**
 * Strategy interface for OCR engines.
 * Each implementation handles a specific OCR provider.
 */
public interface OcrEngine {

    /**
     * @return the engine identifier
     */
    String getEngineId();

    /**
     * @return the engine display name
     */
    String getEngineName();

    /**
     * Perform OCR on a document.
     *
     * @param inputStream the document content
     * @param mimeType    the MIME type
     * @return the OCR result
     */
    OcrResult performOcr(InputStream inputStream, String mimeType);

    /**
     * Check if this engine can handle the given MIME type.
     *
     * @param mimeType the MIME type
     * @return true if supported
     */
    boolean supportsMimeType(String mimeType);

    /**
     * @return the confidence threshold for this engine
     */
    double getConfidenceThreshold();
}
