package com.orientation.orientationapp.docintel.classification.engine;

import com.orientation.orientationapp.docintel.classification.model.ClassificationResult;
import com.orientation.orientationapp.docintel.document.entity.Document;

import java.util.Map;

/**
 * Interface for document classification engines.
 */
public interface DocumentClassifier {

    /**
     * Classify a document based on its content and metadata.
     *
     * @param text     the extracted text
     * @param metadata the document metadata
     * @return the classification result
     */
    ClassificationResult classify(String text, Map<String, Object> metadata);

    /**
     * @return the classifier identifier
     */
    String getClassifierId();
}
