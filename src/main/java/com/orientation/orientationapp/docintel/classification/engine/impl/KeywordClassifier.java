package com.orientation.orientationapp.docintel.classification.engine.impl;

import com.orientation.orientationapp.docintel.classification.engine.DocumentClassifier;
import com.orientation.orientationapp.docintel.classification.model.ClassificationResult;
import com.orientation.orientationapp.docintel.document.entity.Document.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
public class KeywordClassifier implements DocumentClassifier {

    private static final Map<DocumentType, List<String>> KEYWORDS = Map.of(
            DocumentType.ORIENTATION_GUIDE, List.of("orientation", "guide", "orientation universitaire", "admission", "critères"),
            DocumentType.UNIVERSITY_GUIDE, List.of("université", "campus", "faculté", "département"),
            DocumentType.SCHOLARSHIP_GUIDE, List.of("bourse", "scholarship", "financement", "aide financière"),
            DocumentType.REGULATION, List.of("règlement", "règlementation", "loi", "décret", "arrêté"),
            DocumentType.TRANSCRIPT, List.of("relevé de notes", "bulletin", "notes", "moyenne", "mentions"),
            DocumentType.PROGRAM, List.of("programme", "filière", "licence", "master", "diplôme", "curriculum"),
            DocumentType.BROCHURE, List.of("brochure", "présentation", "catalogue", "publicité")
    );

    @Override
    public ClassificationResult classify(String text, Map<String, Object> metadata) {
        String lowerText = text != null ? text.toLowerCase() : "";
        Map<DocumentType, BigDecimal> scores = new EnumMap<>(DocumentType.class);

        for (Map.Entry<DocumentType, List<String>> entry : KEYWORDS.entrySet()) {
            int matchCount = 0;
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    matchCount++;
                }
            }
            BigDecimal score = BigDecimal.valueOf((double) matchCount / entry.getValue().size() * 100);
            scores.put(entry.getKey(), score);
        }

        // Boost score based on metadata
        if (metadata != null) {
            String title = (String) metadata.get("title");
            if (title != null) {
                String lowerTitle = title.toLowerCase();
                for (Map.Entry<DocumentType, List<String>> entry : KEYWORDS.entrySet()) {
                    for (String keyword : entry.getValue()) {
                        if (lowerTitle.contains(keyword.toLowerCase())) {
                            BigDecimal current = scores.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                            scores.put(entry.getKey(), current.add(BigDecimal.TEN));
                        }
                    }
                }
            }
        }

        // Find the best classification
        DocumentType bestType = DocumentType.UNKNOWN;
        BigDecimal bestScore = BigDecimal.ZERO;

        for (Map.Entry<DocumentType, BigDecimal> entry : scores.entrySet()) {
            if (entry.getValue().compareTo(bestScore) > 0) {
                bestScore = entry.getValue();
                bestType = entry.getKey();
            }
        }

        // Normalize scores
        BigDecimal maxScore = scores.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        if (maxScore.compareTo(BigDecimal.ZERO) > 0) {
            for (DocumentType type : scores.keySet()) {
                scores.put(type, scores.get(type).multiply(BigDecimal.valueOf(100)).divide(maxScore, 2, BigDecimal.ROUND_HALF_UP));
            }
        }

        return ClassificationResult.builder()
                .primaryType(bestType)
                .primaryConfidence(bestScore.min(BigDecimal.valueOf(100)))
                .allClassifications(scores)
                .classificationEngine(getClassifierId())
                .features(Map.of("keywordMatches", scores))
                .build();
    }

    @Override
    public String getClassifierId() {
        return "KEYWORD";
    }
}
