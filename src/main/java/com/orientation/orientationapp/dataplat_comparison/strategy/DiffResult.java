package com.orientation.orientationapp.dataplat_comparison.strategy;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiffResult {

    @Builder.Default
    private List<DiffItem> added = new ArrayList<>();

    @Builder.Default
    private List<DiffItem> removed = new ArrayList<>();

    @Builder.Default
    private List<DiffItem> modified = new ArrayList<>();

    @Builder.Default
    private List<DiffItem> unchanged = new ArrayList<>();

    private int totalOldRecords;
    private int totalNewRecords;

    public int getChangeCount() {
        return added.size() + removed.size() + modified.size();
    }

    public boolean hasChanges() {
        return getChangeCount() > 0;
    }
}
