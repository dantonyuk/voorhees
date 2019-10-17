package com.hylamobile.voorhees.client

import com.hylamobile.voorhees.client.annotation.JsonRpcService
import com.hylamobile.voorhees.client.annotation.Param
import com.hylamobile.voorhees.jsonrpc.*
import java.lang.reflect.*
import java.util.*

open class JsonRpcClient(private val transportGroup: TransportGroup) {

    companion object {
        private val TRANSPORT_PROVIDER: TransportProvider?

        init {
            val serviceLoader = ServiceLoader.load(TransportProvider::class.java)
            val iterator = serviceLoader.iterator()
            TRANSPORT_PROVIDER = if (iterator.hasNext()) iterator.next() else null
        }

        @JvmStatic
        fun of(serverConfig: ServerConfig): JsonRpcClient {
            val transportGroup = TRANSPORT_PROVIDER?.transportGroup(serverConfig) ?:
                throw IllegalStateException("JSON RPC Transport provider not found")
            return JsonRpcClient(transportGroup)
        }
    }

    private val errorRegistrar = ErrorRegistrar()

    fun <T> getService(type: Class<T>): T {
        val anno = type.getAnnotation(JsonRpcService::class.java)

        requireNotNull(anno) { "Class ${type.name} should be annotated with JsonRpcService annotation" }
        check(anno.location.isNotEmpty()) { "@JsonRpcService on ${type.name} should have location set" }

        return getService(anno.location, type)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getService(location: String, type: Class<T>): T =
        Proxy.newProxyInstance(type.classLoader, arrayOf(type),
            ServiceProxy(transportGroup(location))) as T

    fun <T : JsonRpcException> registerException(errorCode: Int, exClass: Class<T>) {
        errorRegistrar.registerException(errorCode, exClass)
    }

    inner class ServiceProxy(private val transport: Transport) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            // for debugger
            if (method.name == "toString") {
                return "Just a proxy"
            }

            val jsonRequest = prepareRequest(method, args)
            val jsonResponse = transport.sendRequest(jsonRequest, method.genericReturnType)
            jsonResponse.error?.let(errorRegistrar::handleError)
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
    }
}
