package com.hylamobile.voorhees.client.spring.error;

import com.hylamobile.voorhees.jsonrpc.Error;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

import java.util.List;

public class GeneralException extends JsonRpcException {

    private static final int CODE = 100;

    public GeneralException(String message, List<Integer> userErrorCodes) {
        super(new Error(CODE, message, userErrorCodes));
    }
}
