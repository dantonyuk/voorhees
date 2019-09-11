package com.hylamobile.voorhees.example.client.api;

import com.hylamobile.voorhees.jsonrpc.ErrorCode;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

public class NullObjectException extends JsonRpcException {

    private static final ErrorCode ERROR_CODE = new ErrorCode(3, "Null object");

    public NullObjectException(Object data) {
        super(ERROR_CODE.toError(data));
    }
}
