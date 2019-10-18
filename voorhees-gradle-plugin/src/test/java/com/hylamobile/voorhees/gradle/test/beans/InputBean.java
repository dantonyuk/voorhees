package com.hylamobile.voorhees.gradle.test.beans;

import java.util.List;

public class InputBean {

    private String stringVar;
    private String stringVal;
    private int intVar;
    private int intVal;
    private int[] intArray;
    private TestEnum enumVar;
    private List<IndirectBean> indirectBeans;

    public String getStringVar() {
        return stringVar;
    }

    public void setStringVar(String stringVar) {
        this.stringVar = stringVar;
    }

    public String getStringVal() {
        return stringVal;
    }

    public int getIntVar() {
        return intVar;
    }

    public void setIntVar(int intVar) {
        this.intVar = intVar;
    }

    public int getIntVal() {
        return intVal;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public TestEnum getEnumVar() {
        return enumVar;
    }

    public void setEnumVar(TestEnum enumVar) {
        this.enumVar = enumVar;
    }

    public List<IndirectBean> getIndirectBeans() {
        return indirectBeans;
    }

    public void setIndirectBeans(List<IndirectBean> indirectBeans) {
        this.indirectBeans = indirectBeans;
    }
}
