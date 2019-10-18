package com.hylamobile.voorhees.gradle.test.beans;

import lombok.Data;

import java.util.List;

@Data
public class InputBean {

    private String stringVar;
    private String stringVal;
    private int intVar;
    private int intVal;
    private int[] intArray;
    private TestEnum enumVar;
    private List<IndirectBean> indirectBeans;
}
