package com.hylamobile.voorhees.gradle.test;

import com.hylamobile.voorhees.jsonrpc.Error;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

import java.util.List;

public class RemoteException extends JsonRpcException {

    private static final int CODE = 123;

    public RemoteException(List<String> errorMessages) {
        super(new Error(CODE, "Remote errors", errorMessages));
    }
}
