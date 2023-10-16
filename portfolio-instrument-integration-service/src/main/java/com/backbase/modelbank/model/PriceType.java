package com.backbase.modelbank.model;

public enum PriceType {
    CLOSE(4);

    private final Integer index;

    PriceType(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }
}
