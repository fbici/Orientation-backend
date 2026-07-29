package com.orientation.orientationapp.dataplat_formats.api.event;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportResult;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportCompletedEvent {

    private ImportResult result;
    private boolean success;

    public static ImportCompletedEvent success(ImportResult result) {
        return ImportCompletedEvent.builder()
                .result(result)
                .success(true)
                .build();
    }

    public static ImportCompletedEvent failure(ImportResult result) {
        return ImportCompletedEvent.builder()
                .result(result)
                .success(false)
                .build();
    }
}
