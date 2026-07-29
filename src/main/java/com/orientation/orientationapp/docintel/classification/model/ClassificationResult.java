package com.orientation.orientationapp.docintel.classification.model;

import com.orientation.orientationapp.docintel.document.entity.Document.DocumentType;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationResult {

    private DocumentType primaryType;
    private BigDecimal primaryConfidence;
    private Map<DocumentType, BigDecimal> allClassifications;
    private String classificationEngine;
    private Map<String, Object> features;
}
