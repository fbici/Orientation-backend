package com.orientation.orientationapp.knowledge.entity;
import java.util.UUID;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "knowledge_nodes")
public class KnowledgeNode extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nodeType;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private UUID entityId;

    @Column(length = 50)
    private String entityType;

    @Column(columnDefinition = "jsonb")
    private String properties;

    @Column(columnDefinition = "text")
    private String embedding;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "knowledge_node_relations",
        joinColumns = @JoinColumn(name = "source_id"),
        inverseJoinColumns = @JoinColumn(name = "target_id")
    )
    @Builder.Default
    private Set<KnowledgeNode> relatedNodes = new HashSet<>();
}
