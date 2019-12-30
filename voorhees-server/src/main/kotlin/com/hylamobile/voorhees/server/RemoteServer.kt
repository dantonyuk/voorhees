package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.ErrorCode
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.Response
import com.hylamobile.voorhees.server.annotation.DontExpose
import com.hylamobile.voorhees.util.Option
import java.lang.reflect.Method

class RemoteServer(
    server: Any,
    private val config: RemoteConfig) {

    private val handlerFactory = RemoteHandler.makeFactory(server, config)

    private val serverMethods = server.javaClass.methods
        .filter { it.getAnnotation(DontExpose::class.java) == null }
        .groupBy { findMethodName(it) }
        .mapValues { it.value.map(handlerFactory) }

    fun call(request: Request): Option<Response<*>> {
        val method = findMethod(request)
        return method.call(request)
    }

    private fun findMethod(jsonRequest: Request): RemoteMethod {
        val methods = serverMethods[jsonRequest.method] ?:
            return ErrorCode.METHOD_NOT_FOUND.toMethod("Method ${jsonRequest.method} not found", config)

        val compatibleMethods = methods
            .filter { method -> method.compatibleWith(jsonRequest.params) }
            .ifEmpty {
                return ErrorCode.INVALID_PARAMS.toMethod(
                    "Method ${jsonRequest.method} with arguments ${jsonRequest.params} not found", config)
            }

        return pickBestMethod(compatibleMethods, jsonRequest)
    }

    private fun pickBestMethod(methods: List<RemoteMethod>, @Suppress("UNUSED_PARAMETER") jsonRequest: Request) =
        methods[0]

    private fun findMethodName(method: Method): String {
        return when {
            config.prefix == "" -> method.name
            else -> "${config.prefix}.${method.name}"
        }
    }
}
