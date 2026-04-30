package com.crms.util;

import com.crms.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.function.Function;
import java.util.List;
import java.util.Map;

public final class PaginationHelper {

    private PaginationHelper() {}

    public static int getPage(Map<String, Object> params) {
        return params.containsKey("page") 
            ? Integer.parseInt(params.get("page").toString()) 
            : 0;
    }

    public static int getSize(Map<String, Object> params) {
        return params.containsKey("size") 
            ? Integer.parseInt(params.get("size").toString()) 
            : 20;
    }

    public static String getSort(Map<String, Object> params, String defaultSort) {
        return params.containsKey("sort") 
            ? params.get("sort").toString() 
            : defaultSort;
    }

    public static String getSortDirection(Map<String, Object> params, String defaultDirection) {
        return params.containsKey("direction") 
            ? params.get("direction").toString().toUpperCase() 
            : defaultDirection.toUpperCase();
    }

    public static <T, R> PageResponse<R> buildResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream()
            .map(mapper)
            .toList();
        return PageResponse.<R>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    public static PageRequest buildPageRequest(Map<String, Object> params, String defaultSort) {
        int page = getPage(params);
        int size = getSize(params);
        String sortField = getSort(params, defaultSort);
        String direction = getSortDirection(params, "ASC");
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
    }
}
