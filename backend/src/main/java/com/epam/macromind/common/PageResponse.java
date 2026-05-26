package com.epam.macromind.common;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int totalPages,
        long totalElements
) {
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getTotalPages(),
                springPage.getTotalElements()
        );
    }
}
