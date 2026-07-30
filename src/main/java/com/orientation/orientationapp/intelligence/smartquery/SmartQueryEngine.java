package com.orientation.orientationapp.intelligence.smartquery;

import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.intelligence.learning.LearningEngine;
import com.orientation.orientationapp.modules.knowledge.entity.KnowledgeNode;
import com.orientation.orientationapp.modules.recommendation.entity.Recommendation;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Moteur de Smart Query — recherche intelligente en langage naturel.
 *
 * Analyse une question en langage naturel et génère une réponse
 * basée sur :
 * - Knowledge Graph
 * - Documents OCR
 * - Programmes
 * - Critères
 * - Bourses
 * - Historique d'apprentissage
 *
 * Sans dépendre d'un LLM externe.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartQueryEngine {

    private final KnowledgeGraphService knowledgeGraphService;
    private final LearningEngine learningEngine;

    /**
     * Traite une requête en langage naturel.
     */
    public SmartQueryResult query(String question) {
        log.info("Smart Query: {}", question);
        SmartQueryResult result = new SmartQueryResult();
        result.setQuestion(question);

        // 1. Extraire les mots-clés de la question
        List<String> keywords = extractKeywords(question);
        result.setKeywords(keywords);

        // 2. Rechercher dans le Knowledge Graph
        List<KnowledgeNode> nodes = new ArrayList<>();
        for (String keyword : keywords) {
            nodes.addAll(knowledgeGraphService.search(keyword));
        }
        // Dédupliquer
        nodes = nodes.stream()
                .collect(Collectors.toMap(KnowledgeNode::getId, n -> n, (a, b) -> a))
                .values().stream().collect(Collectors.toList());
        result.setKnowledgeNodes(nodes);

        // 3. Grouper par type
        Map<String, List<KnowledgeNode>> grouped = nodes.stream()
                .collect(Collectors.groupingBy(KnowledgeNode::getType));
        result.setGroupedNodes(grouped);

        // 4. Générer la réponse
        String answer = generateAnswer(question, keywords, grouped);
        result.setAnswer(answer);

        // 5. Trouver les programmes recommandés
        List<String> recommendedPrograms = findRecommendedPrograms(keywords, grouped);
        result.setRecommendedPrograms(recommendedPrograms);

        // 6. Documents sources
        List<String> sources = nodes.stream()
                .map(KnowledgeNode::getSource)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        result.setSources(sources);

        log.info("Smart Query result: {} nodes, {} programs, {} sources",
                nodes.size(), recommendedPrograms.size(), sources.size());

        return result;
    }

    private List<String> extractKeywords(String question) {
        List<String> keywords = new ArrayList<>();
        String lower = question.toLowerCase();

        // Extraire les pays
        String[] countries = {"france", "maroc", "sénégal", "côte d'ivoire", "tunisie", "canada", "bénin", "algérie"};
        for (String country : countries) {
            if (lower.contains(country)) keywords.add(country);
        }

        // Extraire les domaines
        String[] domains = {"informatique", "médecine", "droit", "économie", "ingénieur", "ia", "intelligence artificielle",
                "sciences", "lettres", "arts", "pharmacie", "dentaire", "architecture"};
        for (String domain : domains) {
            if (lower.contains(domain)) keywords.add(domain);
        }

        // Extraire les types de formations
        String[] types = {"licence", "master", "doctorat", "bts", "ingénieur", "bachelor"};
        for (String type : types) {
            if (lower.contains(type)) keywords.add(type);
        }

        // Extraire les bourses
        if (lower.contains("bourse") || lower.contains("scholarship")) keywords.add("bourse");

        // Si aucun mot-clé trouvé, utiliser les mots significatifs
        if (keywords.isEmpty()) {
            String[] words = lower.split("\\s+");
            for (String word : words) {
                if (word.length() > 3 && !isStopWord(word)) {
                    keywords.add(word);
                }
            }
        }

        return keywords;
    }

    private String generateAnswer(String question, List<String> keywords, Map<String, List<KnowledgeNode>> grouped) {
        StringBuilder answer = new StringBuilder();

        // Réponse basée sur les données trouvées
        List<KnowledgeNode> universities = grouped.getOrDefault("UNIVERSITY", Collections.emptyList());
        List<KnowledgeNode> programs = grouped.getOrDefault("PROGRAM", Collections.emptyList());
        List<KnowledgeNode> scholarships = grouped.getOrDefault("SCHOLARSHIP", Collections.emptyList());

        if (!universities.isEmpty()) {
            answer.append("Universités trouvées :\n");
            for (KnowledgeNode uni : universities) {
                answer.append("- ").append(uni.getName());
                if (uni.getMetadata() != null) answer.append(" (").append(uni.getMetadata()).append(")");
                answer.append("\n");
            }
            answer.append("\n");
        }

        if (!programs.isEmpty()) {
            answer.append("Programmes correspondants :\n");
            for (KnowledgeNode prog : programs) {
                answer.append("- ").append(prog.getName()).append("\n");
            }
            answer.append("\n");
        }

        if (!scholarships.isEmpty()) {
            answer.append("Bourses disponibles :\n");
            for (KnowledgeNode sch : scholarships) {
                answer.append("- ").append(sch.getName()).append("\n");
            }
            answer.append("\n");
        }

        if (answer.length() == 0) {
            answer.append("Aucune information trouvée pour cette requête. ");
            answer.append("Essayez avec d'autres termes ou importez des documents supplémentaires.");
        }

        return answer.toString().trim();
    }

    private List<String> findRecommendedPrograms(List<String> keywords, Map<String, List<KnowledgeNode>> grouped) {
        return grouped.getOrDefault("PROGRAM", Collections.emptyList()).stream()
                .map(KnowledgeNode::getName)
                .collect(Collectors.toList());
    }

    private boolean isStopWord(String word) {
        String[] stopWords = {"je", "tu", "il", "elle", "on", "nous", "vous", "ils", "elles",
                "le", "la", "les", "un", "une", "des", "du", "de", "au", "aux",
                "et", "ou", "mais", "donc", "car", "ni", "que", "qui", "quoi",
                "dans", "pour", "par", "avec", "sans", "sous", "sur",
                "veux", "veut", "voudrais", "cherche", "trouve"};
        return Arrays.asList(stopWords).contains(word);
    }

    // --- DTO ---

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SmartQueryResult {
        private String question;
        private List<String> keywords;
        private List<KnowledgeNode> knowledgeNodes;
        private Map<String, List<KnowledgeNode>> groupedNodes;
        private String answer;
        private List<String> recommendedPrograms;
        private List<String> sources;
    }
}
