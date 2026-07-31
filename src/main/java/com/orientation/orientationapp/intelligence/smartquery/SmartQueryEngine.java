package com.orientation.orientationapp.intelligence.smartquery;

import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.knowledge.entity.KnowledgeNode;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartQueryEngine {

    private final KnowledgeGraphService knowledgeGraphService;

    public SmartQueryResult query(String question) {
        log.info("Smart Query: {}", question);
        SmartQueryResult result = new SmartQueryResult();
        result.setQuestion(question);

        List<String> keywords = extractKeywords(question);
        result.setKeywords(keywords);

        List<KnowledgeNode> nodes = new ArrayList<>();
        for (String keyword : keywords) {
            nodes.addAll(knowledgeGraphService.search(keyword));
        }
        nodes = nodes.stream()
                .collect(Collectors.toMap(KnowledgeNode::getId, n -> n, (a, b) -> a))
                .values().stream().collect(Collectors.toList());
        result.setKnowledgeNodes(nodes);

        Map<String, List<KnowledgeNode>> grouped = nodes.stream()
                .collect(Collectors.groupingBy(KnowledgeNode::getNodeType));
        result.setGroupedNodes(grouped);

        String answer = generateAnswer(question, keywords, grouped);
        result.setAnswer(answer);

        List<String> recommendedPrograms = grouped.getOrDefault("PROGRAM", Collections.emptyList()).stream()
                .map(KnowledgeNode::getName).collect(Collectors.toList());
        result.setRecommendedPrograms(recommendedPrograms);

        List<String> sources = nodes.stream()
                .map(n -> n.getProperties())
                .filter(Objects::nonNull)
                .filter(p -> p.contains("source:"))
                .map(p -> p.replaceAll(".*source:", ""))
                .distinct()
                .collect(Collectors.toList());
        result.setSources(sources);

        return result;
    }

    private List<String> extractKeywords(String question) {
        List<String> keywords = new ArrayList<>();
        String lower = question.toLowerCase();
        String[] countries = {"france","maroc","senegal","cote d'ivoire","tunisie","canada","benin","algerie"};
        for (String c : countries) { if (lower.contains(c)) keywords.add(c); }
        String[] domains = {"informatique","medecine","droit","economie","ingenieur","ia","intelligence artificielle","sciences","lettres","arts","pharmacie"};
        for (String d : domains) { if (lower.contains(d)) keywords.add(d); }
        String[] types = {"licence","master","doctorat","bts","ingenieur","bachelor"};
        for (String t : types) { if (lower.contains(t)) keywords.add(t); }
        if (lower.contains("bourse") || lower.contains("scholarship")) keywords.add("bourse");
        if (keywords.isEmpty()) {
            for (String word : lower.split("\\s+")) {
                if (word.length() > 3 && !isStopWord(word)) keywords.add(word);
            }
        }
        return keywords;
    }

    private String generateAnswer(String question, List<String> keywords, Map<String, List<KnowledgeNode>> grouped) {
        StringBuilder answer = new StringBuilder();
        List<KnowledgeNode> unis = grouped.getOrDefault("UNIVERSITY", Collections.emptyList());
        List<KnowledgeNode> progs = grouped.getOrDefault("PROGRAM", Collections.emptyList());
        List<KnowledgeNode> schols = grouped.getOrDefault("SCHOLARSHIP", Collections.emptyList());
        if (!unis.isEmpty()) { answer.append("Universites trouvees:\n"); for (KnowledgeNode u : unis) answer.append("- ").append(u.getName()).append("\n"); answer.append("\n"); }
        if (!progs.isEmpty()) { answer.append("Programmes correspondants:\n"); for (KnowledgeNode p : progs) answer.append("- ").append(p.getName()).append("\n"); answer.append("\n"); }
        if (!schols.isEmpty()) { answer.append("Bourses disponibles:\n"); for (KnowledgeNode s : schols) answer.append("- ").append(s.getName()).append("\n"); answer.append("\n"); }
        if (answer.length() == 0) answer.append("Aucune information trouvee pour cette requete. Importez des documents pour enrichir la base.");
        return answer.toString().trim();
    }

    private boolean isStopWord(String word) {
        return Arrays.asList("je","tu","il","elle","nous","vous","ils","le","la","les","un","une","des","du","de","au","aux",
            "et","ou","mais","donc","car","que","qui","quoi","dans","pour","par","avec","sans","sous","sur",
            "veux","veut","voudrais","cherche","trouve").contains(word);
    }

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
