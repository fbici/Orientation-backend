package com.orientation.orientationapp.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {

    private PaginationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Sort sort = Sort.by(Sort.Direction.fromString(
                sortDirection != null ? sortDirection : "ASC"),
                sortBy != null ? sortBy : "id"
        );

        return PageRequest.of(safePage, safeSize, sort);
    }

    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, null, null);
    }

    public static Pageable createPageable(int page, int size, String sortBy) {
        return createPageable(page, size, sortBy, "ASC");
    }
}
