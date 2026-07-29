package com.orientation.orientationapp.dataplat_comparison.strategy;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldChange {

    private String fieldName;
    private Object oldValue;
    private Object newValue;
    private ChangeType changeType;

    public enum ChangeType {
        VALUE_CHANGED,
        ADDED,
        REMOVED,
        TYPE_CHANGED
    }
}
