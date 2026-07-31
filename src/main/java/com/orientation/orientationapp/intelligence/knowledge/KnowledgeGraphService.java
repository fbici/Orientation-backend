package com.orientation.orientationapp.intelligence.knowledge;

import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService.ExtractedEntities;
import com.orientation.orientationapp.knowledge.entity.KnowledgeNode;
import com.orientation.orientationapp.knowledge.entity.KnowledgeRelation;
import com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository;
import com.orientation.orientationapp.knowledge.repository.KnowledgeRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeRelationRepository relationRepository;

    @Transactional
    public void updateGraph(ExtractedEntities entities, UUID documentId) {
        log.info("Updating Knowledge Graph for document {}", documentId);

        if (entities.getUniversities() != null) {
            for (var uni : entities.getUniversities()) {
                KnowledgeNode node = findOrCreate("UNIVERSITY", uni.getName());
                node.setProperties("source:document:" + documentId);
                nodeRepository.save(node);
            }
        }

        if (entities.getPrograms() != null) {
            for (var prog : entities.getPrograms()) {
                KnowledgeNode node = findOrCreate("PROGRAM", prog.getName());
                node.setProperties("type:" + prog.getType() + ";source:document:" + documentId);
                nodeRepository.save(node);
            }
        }

        if (entities.getSubjects() != null) {
            for (var sub : entities.getSubjects()) {
                KnowledgeNode node = findOrCreate("SUBJECT", sub.getName());
                node.setProperties("source:document:" + documentId);
                nodeRepository.save(node);
            }
        }

        if (entities.getScholarships() != null) {
            for (var sch : entities.getScholarships()) {
                KnowledgeNode node = findOrCreate("SCHOLARSHIP", sch.getName());
                node.setProperties("source:document:" + documentId);
                nodeRepository.save(node);
            }
        }

        if (entities.getLanguages() != null) {
            for (String lang : entities.getLanguages()) {
                findOrCreate("LANGUAGE", lang);
            }
        }

        log.info("Knowledge Graph updated");
    }

    public List<KnowledgeNode> search(String query) {
        return nodeRepository.findByNameContainingIgnoreCase(query);
    }

    public List<KnowledgeNode> getRelatedNodes(UUID nodeId) {
        List<KnowledgeRelation> relations = relationRepository.findBySourceId(nodeId);
        List<KnowledgeNode> related = new ArrayList<>();
        for (KnowledgeRelation rel : relations) {
            if (rel.getTarget() != null) related.add(rel.getTarget());
        }
        return related;
    }

    private KnowledgeNode findOrCreate(String nodeType, String name) {
        List<KnowledgeNode> existing = nodeRepository.findByNodeType(nodeType);
        for (KnowledgeNode n : existing) {
            if (n.getName() != null && n.getName().equalsIgnoreCase(name)) return n;
        }
        KnowledgeNode node = new KnowledgeNode();
        node.setNodeType(nodeType);
        node.setName(name);
        return node;
    }
}
