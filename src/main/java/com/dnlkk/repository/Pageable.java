package com.dnlkk.repository;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pageable {
    private int limit;
    private int offset;
    private int page;
}
