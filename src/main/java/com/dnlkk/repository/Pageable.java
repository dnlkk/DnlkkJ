package com.dnlkk.repository;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pageable {
    private int limit;
    private int offset;
    private int page;
    private Long totalPages;
    private Sort[] sort;

    public static Pageable randomPageable() {
        return Pageable.builder().limit(1).sort(new Sort[]{Sort.randomSort()}).build();
    }
}
