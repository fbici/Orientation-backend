package com.orientation.orientationapp.intelligence.indexing;

import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService.ExtractedEntities;
import com.orientation.orientationapp.modules.knowledge.entity.KnowledgeNode;
import com.orientation.orientationapp.modules.knowledge.repository.KnowledgeNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service d'indexation pour la recherche intelligente.
 *
 * Reconstruit les index de recherche à chaque import de document.
 * Permet la recherche full-text et la Smart Query.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final KnowledgeNodeRepository nodeRepository;

    /**
     * Réindexe les entités extraites pour la recherche.
     */
    @Transactional
    public void reindex(ExtractedEntities entities, UUID documentId) {
        log.info("Reindexing search for document {}", documentId);

        int indexed = 0;

        // Indexer les universités
        if (entities.getUniversities() != null) {
            for (var uni : entities.getUniversities()) {
                addToIndex("UNIVERSITY", uni.getName(), uni.getCountry(), documentId);
                indexed++;
            }
        }

        // Indexer les programmes
        if (entities.getPrograms() != null) {
            for (var prog : entities.getPrograms()) {
                addToIndex("PROGRAM", prog.getName(), prog.getType(), documentId);
                indexed++;
            }
        }

        // Indexer les bourses
        if (entities.getScholarships() != null) {
            for (var sch : entities.getScholarships()) {
                addToIndex("SCHOLARSHIP", sch.getName(), sch.getCurrency(), documentId);
                indexed++;
            }
        }

        log.info("Search reindexed: {} entries for document {}", indexed, documentId);
    }

    private void addToIndex(String type, String name, String metadata, UUID documentId) {
        KnowledgeNode node = new KnowledgeNode();
        node.setType(type);
        node.setName(name);
        node.setMetadata(metadata);
        node.setSource("document:" + documentId);
        nodeRepository.save(node);
    }
}
