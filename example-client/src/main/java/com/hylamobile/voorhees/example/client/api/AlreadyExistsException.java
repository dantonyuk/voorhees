package com.hylamobile.voorhees.example.client.api;

import com.hylamobile.voorhees.jsonrpc.ErrorCode;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

public class AlreadyExistsException extends JsonRpcException {

    private static final ErrorCode ERROR_CODE = new ErrorCode(2, "Already exists");

    public AlreadyExistsException(Object data) {
        super(ERROR_CODE.toError(data));
    }
}
