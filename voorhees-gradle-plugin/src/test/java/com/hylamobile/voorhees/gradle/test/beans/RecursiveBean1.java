package com.hylamobile.voorhees.gradle.test.beans;

public class RecursiveBean1 {

    private RecursiveBean2 subBean;

    public RecursiveBean2 getSubBean() {
        return subBean;
    }

    public void setSubBean(RecursiveBean2 subBean) {
        this.subBean = subBean;
    }
}
