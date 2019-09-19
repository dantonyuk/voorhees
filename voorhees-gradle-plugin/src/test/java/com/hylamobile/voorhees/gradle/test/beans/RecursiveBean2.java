package com.hylamobile.voorhees.gradle.test.beans;

public class RecursiveBean2 {

    private RecursiveBean1 subBean;

    public RecursiveBean1 getSubBean() {
        return subBean;
    }

    public void setSubBean(RecursiveBean1 subBean) {
        this.subBean = subBean;
    }
}
