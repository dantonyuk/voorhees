package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.ErrorCode
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.Response
import com.hylamobile.voorhees.server.annotation.DontExpose

class RemoteServer(server: Any, private val config: RemoteConfig) {

    val handlerFactory = RemoteHandler.makeFactory(server, config)

    val serverMethods = server.javaClass.methods
        .filter { it.getAnnotation(DontExpose::class.java) == null }
        .groupBy { it.name }
        .mapValues { it.value.map(handlerFactory) }

    fun call(request: Request): Response<*> {
        val method = findMethod(request)
        return method.call(request)
    }

    fun findMethod(jsonRequest: Request): RemoteMethod {
        val methods = serverMethods[jsonRequest.method] ?:
            return ErrorCode.METHOD_NOT_FOUND.toMethod("Method ${jsonRequest.method} not found")

        val compatibleMethods = methods
            .filter { method -> method.compatibleWith(jsonRequest.params) }
            .ifEmpty {
                return ErrorCode.INVALID_PARAMS.toMethod(
                    "Method ${jsonRequest.method} with arguments ${jsonRequest.params} not found")
            }

        return pickBestMethod(compatibleMethods, jsonRequest)
    }

    private fun pickBestMethod(methods: List<RemoteMethod>, @Suppress("UNUSED_PARAMETER") jsonRequest: Request) =
        methods[0]
}
