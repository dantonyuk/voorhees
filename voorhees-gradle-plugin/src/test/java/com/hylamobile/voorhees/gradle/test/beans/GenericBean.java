package com.hylamobile.voorhees.gradle.test.beans;

public class GenericBean<T> {

    private T underlying;

    public T getUnderlying() {
        return underlying;
    }

    public void setUnderlying(T underlying) {
        this.underlying = underlying;
    }
}
