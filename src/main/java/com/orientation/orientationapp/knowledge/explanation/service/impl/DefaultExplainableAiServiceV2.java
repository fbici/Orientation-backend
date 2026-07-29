package com.orientation.orientationapp.knowledge.explanation.service.impl;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.orientation.orientationapp.knowledge.explanation.service.ExplainableAiServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DefaultExplainableAiServiceV2 implements ExplainableAiServiceV2 {

    @Override
    public Map<String, Object> explain(UUID recommendationId) {
        log.info("Generating explanation for recommendation: {}", recommendationId);

        Map<String, Object> explanation = new HashMap<>();
        explanation.put("recommendationId", recommendationId);
        explanation.put("headline", "Recommandation basÃƒÂ©e sur l'analyse de votre profil");
        explanation.put("summary", "Votre profil correspond bien aux critÃƒÂ¨res de ce programme");

        List<Map<String, Object>> strengths = List.of(
                Map.of("text", "Moyenne supÃƒÂ©rieure au minimum requis", "impact", "Positif", "icon", "Ã¢Å“â€¦"),
                Map.of("text", "Notes excellentes en matiÃƒÂ¨res clÃƒÂ©s", "impact", "Positif", "icon", "Ã¢Å“â€¦"),
                Map.of("text", "CompÃƒÂ©tences alignÃƒÂ©es avec le programme", "impact", "Positif", "icon", "Ã¢Å“â€¦")
        );
        explanation.put("strengths", strengths);

        List<Map<String, Object>> criteria = List.of(
                Map.of("text", "Moyenne gÃƒÂ©nÃƒÂ©rale >= 12/20", "met", true, "actual", "15.2"),
                Map.of("text", "MathÃƒÂ©matiques >= 14/20", "met", true, "actual", "18"),
                Map.of("text", "Physique >= 12/20", "met", true, "actual", "16")
        );
        explanation.put("criteria", criteria);

        explanation.put("confidence", 0.85);
        explanation.put("rulesUsed", List.of("ADMISSION_CRITERIA", "HISTORICAL_DATA", "COMPETENCY_MATCH"));

        return explanation;
    }

    @Override
    public Map<String, Object> explainWhyRecommended(UUID candidateId, UUID programId) {
        log.info("Explaining why program {} recommended for candidate {}", programId, candidateId);

        Map<String, Object> explanation = new HashMap<>();
        explanation.put("programId", programId);
        explanation.put("candidateId", candidateId);
        explanation.put("reasons", List.of(
                Map.of("reason", "Profil acadÃƒÂ©mique fort", "weight", 0.4),
                Map.of("reason", "CompÃƒÂ©tences alignÃƒÂ©es", "weight", 0.3),
                Map.of("reason", "Historique favorable", "weight", 0.2),
                Map.of("reason", "PrÃƒÂ©fÃƒÂ©rences utilisateur", "weight", 0.1)
        ));
        explanation.put("score", 82.5);

        return explanation;
    }

    @Override
    public Map<String, Object> explainWhyNotRecommended(UUID candidateId, UUID programId) {
        log.info("Explaining why program {} NOT recommended for candidate {}", programId, candidateId);

        Map<String, Object> explanation = new HashMap<>();
        explanation.put("programId", programId);
        explanation.put("candidateId", candidateId);
        explanation.put("reasons", List.of(
                Map.of("reason", "Moyenne insuffisante", "expected", ">=16", "actual", "14.2"),
                Map.of("reason", "Note en Math trop faible", "expected", ">=15", "actual", "12"),
                Map.of("reason", "CompÃƒÂ©tences insuffisantes", "detail", "Niveau requis non atteint")
        ));
        explanation.put("score", 45.0);

        return explanation;
    }
}
