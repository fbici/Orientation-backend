package com.orientation.orientationapp.knowledge.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "knowledge_similarity_matrix")
public class SimilarityMatrix extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false)
    private UUID entityIdA;

    @Column(nullable = false)
    private UUID entityIdB;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal similarityScore;

    @Column(length = 50)
    private String algorithm;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
