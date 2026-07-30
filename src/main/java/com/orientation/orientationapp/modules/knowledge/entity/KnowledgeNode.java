package com.orientation.orientationapp.modules.knowledge.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * Entité Knowledge Node — nœud du Knowledge Graph.
 *
 * Représente une connaissance extraite d'un document :
 * - UNIVERSITÉ : une université
 * - PROGRAM : un programme de formation
 * - SUBJECT : une matière
 * - SCHOLARSHIP : une bourse
 * - LANGUAGE : une langue
 * - CONDITION : une condition (visa, logement, etc.)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "knowledge_nodes")
public class KnowledgeNode extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String metadata;

    @Column(length = 500)
    private String source;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
