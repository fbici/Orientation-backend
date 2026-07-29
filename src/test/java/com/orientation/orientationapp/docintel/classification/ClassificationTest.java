package com.orientation.orientationapp.docintel.classification;

import com.orientation.orientationapp.docintel.classification.engine.impl.KeywordClassifier;
import com.orientation.orientationapp.docintel.classification.model.ClassificationResult;
import com.orientation.orientationapp.docintel.document.entity.Document.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassificationTest {

    private KeywordClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new KeywordClassifier();
    }

    @Test
    void shouldClassifyOrientationGuide() {
        String text = "Guide d'orientation universitaire 2025. Critères d'admission pour les nouvelles filières.";
        ClassificationResult result = classifier.classify(text, Map.of("title", "Guide Orientation"));

        assertNotNull(result);
        assertEquals(DocumentType.ORIENTATION_GUIDE, result.getPrimaryType());
        assertTrue(result.getPrimaryConfidence().doubleValue() > 0);
    }

    @Test
    void shouldClassifyTranscript() {
        String text = "Relevé de notes - Année 2025. Moyenne générale: 15.2. Mention: Bien.";
        ClassificationResult result = classifier.classify(text, null);

        assertNotNull(result);
        assertEquals(DocumentType.TRANSCRIPT, result.getPrimaryType());
    }

    @Test
    void shouldClassifyProgram() {
        String text = "Programme de Licence en Informatique. Durée: 3 ans. Filière: Sciences.";
        ClassificationResult result = classifier.classify(text, null);

        assertNotNull(result);
        assertEquals(DocumentType.PROGRAM, result.getPrimaryType());
    }

    @Test
    void shouldReturnClassifierId() {
        assertEquals("KEYWORD", classifier.getClassifierId());
    }

    @Test
    void shouldHandleEmptyText() {
        ClassificationResult result = classifier.classify("", null);
        assertNotNull(result);
        assertNotNull(result.getPrimaryType());
    }
}
