package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.Error
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.Response

class RemoteError(private val error: Error) : RemoteMethod {

    override fun call(request: Request): Response<*> =
        Response.error(error, request.id)
}
