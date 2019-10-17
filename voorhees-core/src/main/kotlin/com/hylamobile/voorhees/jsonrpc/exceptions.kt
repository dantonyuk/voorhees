package com.hylamobile.voorhees.jsonrpc

open class JsonRpcException(
    val error: Error, cause: Throwable? = null) :
    RuntimeException(error.message, cause) {

    // for Java
    constructor(error: Error) : this(error, null)
}

class CustomJsonRpcException(error: Error) : JsonRpcException(error)

class ParseErrorException(data: Any? = null) :
    JsonRpcException(ErrorCode.PARSE_ERROR.toError(data))

class InvalidRequestException(data: Any? = null) :
    JsonRpcException(ErrorCode.INVALID_REQUEST.toError(data))

class MethodNotFoundException(data: Any? = null) :
    JsonRpcException(ErrorCode.METHOD_NOT_FOUND.toError(data))

class InvalidParamsException(data: Any? = null) :
    JsonRpcException(ErrorCode.INVALID_PARAMS.toError(data))

open class InternalErrorException(data: Any? = null) :
    JsonRpcException(ErrorCode.INTERNAL_ERROR.toError(data)) {

    // for Java
    constructor() : this(null)
}
