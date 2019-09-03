package com.hylamobile.voorhees.jsonrpc

sealed class JsonRpcException(
    val error: Error, cause: Throwable? = null) :
    RuntimeException(error.message, cause)

class CustomJsonRpcException(error: Error) : JsonRpcException(error)

class InvalidRequestException(data: Any? = null) :
    JsonRpcException(ErrorCode.INVALID_REQUEST.toError(data))

class MethodNotFoundException(data: Any? = null) :
    JsonRpcException(ErrorCode.METHOD_NOT_FOUND.toError(data))

class InvalidParamsException(data: Any? = null) :
    JsonRpcException(ErrorCode.INVALID_PARAMS.toError(data))

class InternalErrorException(data: Any? = null) :
    JsonRpcException(ErrorCode.INTERNAL_ERROR.toError(data))
