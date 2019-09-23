package com.hylamobile.voorhees.client.spring;

import com.hylamobile.voorhees.client.annotation.JsonRpcService;

@JsonRpcService(location = "/first")
public interface FirstService {

    int plus(int l, int r);
}
