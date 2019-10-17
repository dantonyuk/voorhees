package com.hylamobile.voorhees.client.spring.user;

import com.hylamobile.voorhees.jsonrpc.Error;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

import java.util.List;

public class UserException extends JsonRpcException {

    private static final int CODE = 42;

    public UserException(String message, List<Integer> userErrorCodes) {
        super(new Error(CODE, message, userErrorCodes));
    }
}
