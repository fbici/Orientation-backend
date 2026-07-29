package com.orientation.orientationapp.dataplat_formats.api.event;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportStartedEvent {

    private ImportContext context;
    private String initiatedBy;

    public static ImportStartedEvent of(ImportContext context, String user) {
        return ImportStartedEvent.builder()
                .context(context)
                .initiatedBy(user)
                .build();
    }
}
