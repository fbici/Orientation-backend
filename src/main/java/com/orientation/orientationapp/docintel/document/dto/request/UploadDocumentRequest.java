package com.orientation.orientationapp.docintel.document.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadDocumentRequest {
    private String title;
    private String description;
    private String tenantId;
}
