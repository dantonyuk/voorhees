package com.hylamobile.voorhees.client

import com.github.kittinunf.fuel.core.RequestExecutionOptions
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.hylamobile.voorhees.client.annotation.JsonRpcService
import com.hylamobile.voorhees.client.annotation.Param
import com.hylamobile.voorhees.jsonrpc.*
import com.hylamobile.voorhees.util.uriCombine
import java.lang.reflect.*
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
        val path = uriCombine(serverConfig.url, location)
        return Proxy.newProxyInstance(type.classLoader, arrayOf(type), ServiceProxy(path)) as T
    }

    open fun updateOptions(options: RequestExecutionOptions) {
        serverConfig.connectTimeout?.also { options.timeoutInMillisecond = it }
        serverConfig.readTimeout?.also { options.timeoutReadInMillisecond = it }
    }

    inner class ServiceProxy(private val endpoint: String) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            fun responseType() =
                object : ParameterizedType {
                    override fun getRawType(): Type = Response::class.java
                    override fun getOwnerType(): Type? = null
                    override fun getActualTypeArguments(): Array<Type> = arrayOf(
                        when (val retType = method.genericReturnType) {
                            Void.TYPE -> Object::class.java
                            else -> retType
                        })
                }

            // for debugger
            if (method.name == "toString") {
                return "Just a proxy"
            }

            val jsonRequest = prepareRequest(method, args)
            val httpResult = sendRequest(jsonRequest)
            val jsonRepr = httpResult.get().toString(Charset.forName("UTF-8"))
            val jsonResponse = jsonRepr.parseJsonAs(responseType()) as Response<*>
            jsonResponse.error?.let { error -> throw CustomJsonRpcException(error) }
            return jsonResponse.result
        }

        // private region

        private fun prepareRequest(method: Method, args: Array<out Any?>?): Request {

            fun Parameter.paramAnno() = getAnnotation(Param::class.java)

            fun <T> Array<out T?>.asJsonSeq() = asSequence().map(Any?::jsonTree)

            fun onlyNamedParameters() = method.parameters.all { it.paramAnno() != null }

            val params = when {
                args == null -> null
                onlyNamedParameters() ->
                    ByNameParams(method.parameters.asSequence()
                        .map { it.paramAnno().name }
                        .zip(args.asJsonSeq())
                        .toMap())
                else ->
                    ByPositionParams(args.asJsonSeq().toList())
            }

            return Request(method = method.name, params = params, id = NumberId(1))
        }

        private fun sendRequest(jsonRequest: Request) =
            endpoint.httpPost()
                .apply { this@JsonRpcClient.updateOptions(executionOptions) }
                .jsonBody(jsonRequest.jsonString)
                .response()
                .third
    }
}
