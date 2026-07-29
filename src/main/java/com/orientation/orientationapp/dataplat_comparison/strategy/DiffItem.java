package com.orientation.orientationapp.dataplat_comparison.strategy;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiffItem {

    private String key;
    private DiffType type;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private Map<String, FieldChange> fieldChanges;

    public enum DiffType {
        ADDED, REMOVED, MODIFIED, UNCHANGED
    }
}
