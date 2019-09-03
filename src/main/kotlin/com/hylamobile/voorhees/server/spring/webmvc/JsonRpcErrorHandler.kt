package com.hylamobile.voorhees.server.spring.webmvc

import com.hylamobile.voorhees.jsonrpc.JsonRpcException
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.Response
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonRpcErrorHandler(private val ex: JsonRpcException) : JsonRpcHandler {

    override fun handleRequest(httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {
        val jsonRequest = httpRequest.getAttribute(JsonRpcMethodHandler.REQ_ATTR_NAME) as Request? ?:
            throw IllegalStateException("Can not find ${JsonRpcMethodHandler.REQ_ATTR_NAME} in request attributes")

        val jsonResponse = Response.error(ex.error, jsonRequest.id)
        JsonRpcResponse(jsonResponse).respondTo(httpResponse)
    }
}
