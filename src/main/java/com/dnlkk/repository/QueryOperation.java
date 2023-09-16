package com.dnlkk.repository;

public enum QueryOperation {
    FIND("find"),
    COUNT("count"),
    SUM("sum"),
    SAVE("save"),
    DELETE("delete");

    private String value;

    private QueryOperation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}