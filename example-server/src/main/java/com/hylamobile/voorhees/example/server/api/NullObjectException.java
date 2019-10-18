package com.hylamobile.voorhees.example.server.api;

import com.hylamobile.voorhees.jsonrpc.Error;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

public class NullObjectException extends JsonRpcException {

    private static final int CODE = 3;

    public NullObjectException(String message, String title) {
        super(new Error(CODE, message, title));
    }
}
