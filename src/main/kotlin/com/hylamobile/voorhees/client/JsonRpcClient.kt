package com.hylamobile.voorhees.client

import com.fasterxml.jackson.databind.JsonNode
import com.github.kittinunf.fuel.core.RequestExecutionOptions
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.hylamobile.voorhees.client.annotation.JsonRpcService
import com.hylamobile.voorhees.client.annotation.Param
import com.hylamobile.voorhees.jsonrpc.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Proxy
import java.nio.charset.Charset

data class ServerConfig(
    val url: String,
    var connectTimeout: Int? = null,
    var readTimeout: Int? = null) {
    // for Java
    constructor(url: String) : this(url, null, null)
}

open class JsonRpcClient(private val serverConfig: ServerConfig) {

    fun <T> getService(type: Class<T>): T {
        val anno = type.getAnnotation(JsonRpcService::class.java)

        requireNotNull(anno) { "Class ${type.name} should be annotated with JsonRpcService annotation" }
        check(anno.location.isNotEmpty()) { "@JsonRpcService on ${type.name} should have location set" }

        return getService(anno.location, type)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getService(location: String, type: Class<T>): T {
        val url = serverConfig.url + if (serverConfig.url.endsWith("/")) "" else "/"
        val loc = if (location.startsWith("/")) location.substring(1) else location
        return Proxy.newProxyInstance(type.classLoader, arrayOf(type), ServiceProxy(url + loc)) as T
    }

    open fun updateOptions(options: RequestExecutionOptions) {
        serverConfig.connectTimeout?.also { options.timeoutInMillisecond = it }
        serverConfig.readTimeout?.also { options.timeoutReadInMillisecond = it }
    }

    inner class ServiceProxy(private val endpoint: String) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            // for debugger
            if (method.name == "toString") {
                return "Just a proxy"
            }

            val params = when {
                args == null -> null
                method.hasOnlyNamedParameters() -> prepareNamedParams(method, args)
                else -> preparePosParams(args)
            }

            val jsonRequest = Request(method = method.name, params = params)

            val (_, _, result) = endpoint.httpPost()
                .apply { this@JsonRpcClient.updateOptions(executionOptions) }
                .jsonBody(Json.serializeRequest(jsonRequest))
                .response()

            return result
                .fold({
                    val repr = it.toString(Charset.forName("UTF-8"))
                    val json = Json.parseTree(repr)
                    val resp = Json.parse<Response<*>>(repr, Response::class.java)
                    resp.error?.let { ex -> throw CustomJsonRpcException(ex) }
                    val jsonResult = json?.get("result") ?: throw NullPointerException()
                    Json.parseNode(jsonResult, method.returnType)
                }, {
                    throw it
                })
        }

        // private region

        private fun Method.hasOnlyNamedParameters() =
            parameters.all { it.paramAnno != null }

        private fun preparePosParams(args: Array<out Any?>) =
            ByPositionParams(args.asJsonSeq().toList())

        private fun prepareNamedParams(method: Method, args: Array<out Any?>) =
            ByNameParams(method.parameters.asSequence()
                .map { it.paramAnno.name }
                .zip(args.asJsonSeq())
                .toMap())

        // extensions

        private fun <T> Array<out T?>.asJsonSeq() =
            asSequence().map { Json.objectMapper.valueToTree<JsonNode>(it) }

        private val Parameter.paramAnno
            get() = getAnnotation(Param::class.java)
    }
}
