package com.orientation.orientationapp.knowledge.entity;
import java.util.UUID;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "knowledge_search_index")
public class SearchIndex extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false)
    private UUID entityId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String content;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(columnDefinition = "text")
    private String embedding;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
