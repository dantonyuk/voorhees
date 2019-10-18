package com.hylamobile.voorhees.example.server.api;

import com.hylamobile.voorhees.jsonrpc.Error;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

public class NotFoundException extends JsonRpcException {

    private static final int CODE = 1;

    public NotFoundException(String message, String entity) {
        super(new Error(CODE, message, entity));
    }
}
