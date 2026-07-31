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

import java.util.*;

/**
 * Service de gestion du Knowledge Graph.
 *
 * Met à jour le graphe de connaissances à chaque import de document.
 * Crée les nœuds (universités, programmes, matières, bourses, critères)
 * et les relations entre eux.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeRelationRepository relationRepository;

    /**
     * Met à jour le Knowledge Graph avec les entités extraites d'un document.
     */
    @Transactional
    public void updateGraph(ExtractedEntities entities, UUID documentId) {
        log.info("Updating Knowledge Graph for document {}", documentId);

        // Créer les nœuds universités
        if (entities.getUniversities() != null) {
            for (var uni : entities.getUniversities()) {
                KnowledgeNode node = findOrCreateNode("UNIVERSITY", uni.getName(), uni.getCountry());
                node.setSource("document:" + documentId);
                nodeRepository.save(node);
                log.debug("Knowledge node UNIVERSITY: {}", uni.getName());
            }
        }

        // Créer les nœuds programmes
        if (entities.getPrograms() != null) {
            for (var prog : entities.getPrograms()) {
                KnowledgeNode node = findOrCreateNode("PROGRAM", prog.getName(), prog.getType());
                node.setSource("document:" + documentId);
                nodeRepository.save(node);

                // Relation programme → université
                if (prog.getUniversity() != null) {
                    KnowledgeNode uniNode = findOrCreateNode("UNIVERSITY", prog.getUniversity(), null);
                    createRelation(node, uniNode, "BELONGS_TO");
                }
                log.debug("Knowledge node PROGRAM: {}", prog.getName());
            }
        }

        // Créer les nœuds matières
        if (entities.getSubjects() != null) {
            for (var sub : entities.getSubjects()) {
                KnowledgeNode node = findOrCreateNode("SUBJECT", sub.getName(), null);
                node.setSource("document:" + documentId);
                nodeRepository.save(node);
                log.debug("Knowledge node SUBJECT: {}", sub.getName());
            }
        }

        // Créer les nœuds bourses
        if (entities.getScholarships() != null) {
            for (var sch : entities.getScholarships()) {
                KnowledgeNode node = findOrCreateNode("SCHOLARSHIP", sch.getName(), sch.getCurrency());
                node.setSource("document:" + documentId);
                nodeRepository.save(node);
                log.debug("Knowledge node SCHOLARSHIP: {}", sch.getName());
            }
        }

        // Créer les nœuds langues
        if (entities.getLanguages() != null) {
            for (String lang : entities.getLanguages()) {
                KnowledgeNode node = findOrCreateNode("LANGUAGE", lang, null);
                node.setSource("document:" + documentId);
                nodeRepository.save(node);
            }
        }

        // Créer les nœuds conditions
        if (entities.getConditions() != null) {
            for (String cond : entities.getConditions()) {
                KnowledgeNode node = findOrCreateNode("CONDITION", cond, null);
                node.setSource("document:" + documentId);
                nodeRepository.save(node);
            }
        }

        log.info("Knowledge Graph updated: {} nodes, {} relations",
                nodeRepository.count(), relationRepository.count());
    }

    /**
     * Recherche dans le Knowledge Graph.
     */
    public List<KnowledgeNode> search(String query) {
        return nodeRepository.findByNameContainingIgnoreCase(query);
    }

    /**
     * Trouve les nœuds liés à un nœud donné.
     */
    public List<KnowledgeNode> getRelatedNodes(UUID nodeId) {
        List<KnowledgeRelation> relations = relationRepository.findBySourceIdOrTargetId(nodeId, nodeId);
        List<KnowledgeNode> related = new ArrayList<>();
        for (KnowledgeRelation rel : relations) {
            UUID relatedId = rel.getSourceId().equals(nodeId) ? rel.getTargetId() : rel.getSourceId();
            nodeRepository.findById(relatedId).ifPresent(related::add);
        }
        return related;
    }

    private KnowledgeNode findOrCreateNode(String type, String name, String metadata) {
        return nodeRepository.findByTypeAndName(type, name)
                .orElseGet(() -> {
                    KnowledgeNode node = new KnowledgeNode();
                    node.setType(type);
                    node.setName(name);
                    node.setMetadata(metadata);
                    return node;
                });
    }

    private void createRelation(KnowledgeNode source, KnowledgeNode target, String type) {
        // Vérifier si la relation existe déjà
        boolean exists = relationRepository.existsBySourceIdAndTargetIdAndType(
                source.getId(), target.getId(), type);
        if (!exists) {
            KnowledgeRelation rel = new KnowledgeRelation();
            rel.setSourceId(source.getId());
            rel.setTargetId(target.getId());
            rel.setType(type);
            relationRepository.save(rel);
        }
    }
}
