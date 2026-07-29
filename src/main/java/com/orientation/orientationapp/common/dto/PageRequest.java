package com.orientation.orientationapp.common.dto;

import com.orientation.orientationapp.common.constant.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Builder.Default
    private int page = AppConstants.DEFAULT_PAGE;

    @Builder.Default
    private int size = AppConstants.DEFAULT_PAGE_SIZE;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "ASC";
}
