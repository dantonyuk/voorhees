package com.hylamobile.voorhees.client.spring.user;

import com.hylamobile.voorhees.client.annotation.JsonRpcService;

@JsonRpcService(location = "/first")
public interface FirstUserService {

    int plus(int l, int r);
}
