package com.orientation.orientationapp.intelligence.indexing;

import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService.ExtractedEntities;
import com.orientation.orientationapp.knowledge.entity.KnowledgeNode;
import com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final KnowledgeNodeRepository nodeRepository;

    @Transactional
    public void reindex(ExtractedEntities entities, UUID documentId) {
        log.info("Reindexing search for document {}", documentId);
        int indexed = 0;

        if (entities.getUniversities() != null) {
            for (var uni : entities.getUniversities()) {
                addToIndex("UNIVERSITY", uni.getName(), documentId);
                indexed++;
            }
        }
        if (entities.getPrograms() != null) {
            for (var prog : entities.getPrograms()) {
                addToIndex("PROGRAM", prog.getName(), documentId);
                indexed++;
            }
        }
        if (entities.getScholarships() != null) {
            for (var sch : entities.getScholarships()) {
                addToIndex("SCHOLARSHIP", sch.getName(), documentId);
                indexed++;
            }
        }
        log.info("Search reindexed: {} entries for document {}", indexed, documentId);
    }

    private void addToIndex(String nodeType, String name, UUID documentId) {
        KnowledgeNode node = new KnowledgeNode();
        node.setNodeType(nodeType);
        node.setName(name);
        node.setProperties("source:document:" + documentId);
        nodeRepository.save(node);
    }
}
