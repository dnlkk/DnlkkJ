package com.dnlkk.repository;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;

@Data
@Builder
public class Sort {
    private String by;
    private SortHow how;

    @Getter
    public enum SortHow {
        DESC("desc"),
        ASC("asc");

        private final String title;
        SortHow(String title) {
            this.title = title;
        }

    }

    public Sort() {
        this("id");
    }

    public Sort(String by) {
        this.by = by;
        this.how = SortHow.ASC;
    }
    public Sort(String[] queryParam) {
        if (queryParam.length == 1) {
            this.by = queryParam[0];
            this.how = SortHow.ASC;
        } else if (queryParam.length == 2) {
            this.by = queryParam[0];
            this.how = SortHow.valueOf(queryParam[1].toUpperCase());
        } else
            throw new RuntimeException("Only 2 params for sort");
    }

    public Sort(String by, SortHow how) {
        this.by = by;
        this.how = how;
    }
    public Sort(String by, String how) {
        this.by = by;
        this.how = SortHow.valueOf(how.toUpperCase());
    }
}
