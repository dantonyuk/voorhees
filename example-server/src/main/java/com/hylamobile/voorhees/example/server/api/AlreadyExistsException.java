package com.hylamobile.voorhees.example.server.api;

import com.hylamobile.voorhees.example.server.domain.Movie;
import com.hylamobile.voorhees.jsonrpc.Error;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;

public class AlreadyExistsException extends JsonRpcException {

    private static final int CODE = 2;

    public AlreadyExistsException(String message, Movie movie) {
        super(new Error(CODE, message, movie));
    }
}
