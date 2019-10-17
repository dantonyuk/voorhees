package com.hylamobile.voorhees.jsonrpc

/**
 * Just a fake class to not depend on voorhees-core
 */
open class JsonRpcException(
    val error: Error, cause: Throwable? = null) :
    RuntimeException(error.message, cause) {

    // for Java
    constructor(error: Error) : this(error, null)
}

