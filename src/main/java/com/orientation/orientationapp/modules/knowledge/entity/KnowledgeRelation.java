package com.orientation.orientationapp.modules.knowledge.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * Entité Knowledge Relation — relation entre deux nœuds du Knowledge Graph.
 *
 * Exemples :
 * - Programme BELONGS_TO Université
 * - Programme REQUIRES Subject
 * - Scholarship AVAILABLE_FOR Program
 * - University LOCATED_IN Country
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "knowledge_relations")
public class KnowledgeRelation extends BaseEntity {

    @Column(nullable = false)
    private UUID sourceId;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(length = 500)
    private String metadata;
}
