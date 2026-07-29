package com.orientation.orientationapp.dataplat_import.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "import_entity_mappings")
public class ImportEntityMapping extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DataType dataType;

    @Column(nullable = false, length = 200)
    private String entityClassName;

    @Column(nullable = false, length = 200)
    private String repositoryClassName;

    @Column(columnDefinition = "jsonb")
    private String columnMappingJson;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
