package com.hylamobile.voorhees.client

import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.Response
import com.hylamobile.voorhees.jsonrpc.parseJsonAs
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface TransportProvider {

    fun transport(serverConfig: ServerConfig): Transport
}

interface Transport {

    fun sendRequest(endpoint: String, request: Request, retType: Type): Response<*> {
        val jsonRepr = getResponseAsString(endpoint, request)
        return jsonRepr.parseJsonAs(responseType(retType)) as Response<*>
    }

    fun getResponseAsString(endpoint: String, request: Request): String

    private fun responseType(retType: Type) =
        object : ParameterizedType {
            override fun getRawType(): Type = Response::class.java
            override fun getOwnerType(): Type? = null
            override fun getActualTypeArguments(): Array<Type> = arrayOf(
                when (retType) {
                    Void.TYPE -> Object::class.java
                    else -> retType
                })
        }
}
