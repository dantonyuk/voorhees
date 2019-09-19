package com.hylamobile.voorhees.gradle.test;

import com.hylamobile.voorhees.gradle.test.beans.GenericBean;
import com.hylamobile.voorhees.gradle.test.beans.InputBean;
import com.hylamobile.voorhees.gradle.test.beans.OutputBean;
import com.hylamobile.voorhees.gradle.test.beans.RecursiveBean1;
import com.hylamobile.voorhees.server.annotation.JsonRpcService;

import java.util.List;
import java.util.Map;

@JsonRpcService(locations = "/api")
public class RemoteService {

    public OutputBean handle(InputBean bean) {
        return null;
    }

    public GenericBean<OutputBean> handleGenerics(GenericBean<InputBean> input) {
        return null;
    }

    public List<OutputBean> handleCollections(Map<String, InputBean> input) {
        return null;
    }

    public RecursiveBean1 handleRecursive() {
        return null;
    }
}

