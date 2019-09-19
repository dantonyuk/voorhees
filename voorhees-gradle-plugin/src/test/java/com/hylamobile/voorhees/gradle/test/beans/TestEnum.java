package com.hylamobile.voorhees.gradle.test.beans;

public enum TestEnum {
    FIRST(1), SECOND(2), THIRD(3);

    private final int order;

    TestEnum(int order) {
        this.order = order;
    }
}
