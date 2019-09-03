package com.hylamobile.voorhees.client

import com.fasterxml.jackson.databind.JsonNode
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.interceptors.LogRequestAsCurlInterceptor
import com.github.kittinunf.fuel.httpPost
import com.hylamobile.voorhees.client.annotation.JsonRpcService
import com.hylamobile.voorhees.client.annotation.Param
import com.hylamobile.voorhees.jsonrpc.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.nio.charset.Charset

data class ServerConfig(val url: String)

class JsonRpcClient(private val serverConfig: ServerConfig) {

    fun <T> getService(type: Class<T>): T {
        val anno = type.getAnnotation(JsonRpcService::class.java)

        requireNotNull(anno) { "Class ${type.name} should be annotated with JsonRpcService annotation" }
        check(anno.location.isNotEmpty()) { "@JsonRpcService on ${type.name} should have location set" }

        return getService(anno.location, type)
    }

    fun <T> getService(location: String, type: Class<T>): T =
        Proxy.newProxyInstance(type.classLoader,
            arrayOf(type), ServiceProxy(serverConfig.url + location)) as T

    class ServiceProxy(private val endpoint: String) : InvocationHandler {
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

            FuelManager.instance.addRequestInterceptor(LogRequestAsCurlInterceptor)
            val (_, _, result) = endpoint.httpPost()
                .jsonBody(Json.serializeRequest(jsonRequest))
                .response()
            return result
                .fold({
                    val repr = it.toString(Charset.forName("UTF-8"))
                    val json = Json.parseTree(repr)
                    val resp = Json.parse<Response<*>>(repr, Response::class.java)
                    resp.error?.let { throw CustomJsonRpcException(it) }
                    val jsonResult = json?.get("result") ?: throw NullPointerException()
                    Json.parseNode(jsonResult, method.returnType)
                }, {
                    throw it
                })
        }

        private fun Method.hasOnlyNamedParameters() =
            parameters.all { it.getAnnotation(Param::class.java) != null }

        private fun preparePosParams(args: Array<out Any?>) =
            ByPositionParams(args.asJsonSeq().toList())

        private fun prepareNamedParams(method: Method, args: Array<out Any?>) =
            ByNameParams(method.parameters.asSequence()
                .map { it.getAnnotation(Param::class.java) }
                .map { it.name }
                .zip(args.asJsonSeq())
                .toMap())

        private fun <T> Array<out T?>.asJsonSeq() =
            asSequence().map { Json.objectMapper.valueToTree<JsonNode>(it) }
    }
}
