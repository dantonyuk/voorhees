package com.hylamobile.voorhees.example.server.api;

import com.hylamobile.voorhees.jsonrpc.ErrorCode;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

public class NotFoundException extends JsonRpcException {

    private static final ErrorCode ERROR_CODE = new ErrorCode(1, "Not found");

    public NotFoundException(Object data) {
        super(ERROR_CODE.toError(data));
    }
}
