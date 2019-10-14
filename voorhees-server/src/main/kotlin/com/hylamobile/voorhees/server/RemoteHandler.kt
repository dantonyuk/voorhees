package com.hylamobile.voorhees.server

import com.fasterxml.jackson.databind.JsonNode
import com.hylamobile.voorhees.jsonrpc.*
import com.hylamobile.voorhees.server.annotation.optionDefaultValue
import com.hylamobile.voorhees.server.annotation.paramAnno
import com.hylamobile.voorhees.util.Option
import com.hylamobile.voorhees.util.toArray
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Type

class RemoteHandler(
    private val server: Any,
    private val method: Method,
    private val config: RemoteConfig) : RemoteMethod {

    companion object {
        data class MethodParameter(
            val name: String,
            val type: Type,
            val defaultValue: Option<Any?> = Option.none()
        )

        fun makeFactory(server: Any, config: RemoteConfig): (Method) -> RemoteHandler =
            { method -> RemoteHandler(server, method, config) }
    }

    override val notificationExecutor: NotificationExecutor
        get() = config.notificationExecutor

    private val paramByName: LinkedHashMap<String, MethodParameter> =
        method.parameters
            .zip(config.parameterNameDiscoverer.parameterNames(method) ?: emptyArray())
            .map { (param, discoveredName) ->
                val anno = param.paramAnno
                val name = anno?.name?.ifBlank { null } ?: discoveredName
                val type = param.parameterizedType
                val defaultValue = anno?.optionDefaultValue?.map {
                    try { it.parseJsonAs(param.type) }
                    catch (ex: java.io.IOException) { it }
                } ?: Option.none()
                MethodParameter(name, type, defaultValue)
            }
            .map { it.name to it }
            .toMap(LinkedHashMap())

    private val parameters = paramByName.values

    private val paramNames = parameters.map { it.name }

    private val requiredParamNames =
        parameters.filter { it.defaultValue.isEmpty }.map { it.name }

    override fun invoke(request: Request): Response<Any> {
        val args = convertToArguments(request.params)

        try {
            val result = method.invoke(server, *args)
            return Response.success(result, request.id)
        } catch (ex: InvocationTargetException) {
            ex.printStackTrace()
            val targetEx = ex.targetException
            val error =
                if (targetEx is JsonRpcException) targetEx.error
                else ErrorCode.INTERNAL_ERROR.toError(targetEx.message)

            return Response.error(error, request.id)
        }
    }

    fun compatibleWith(requestParams: Params?): Boolean {
        fun checkNull() = requiredParamNames.isEmpty()

        fun checkPos(requestParams: ByPositionParams) =
            requestParams.params.size.let {
                requiredParamNames.size <= it && it <= method.parameterCount
            }

        fun checkNamed(requestParams: ByNameParams) =
            requestParams.params.keys.let {
                it.containsAll(requiredParamNames) && paramNames.containsAll(it)
            }

        return when (requestParams) {
            null -> checkNull()
            is ByPositionParams -> checkPos(requestParams)
            is ByNameParams -> checkNamed(requestParams)
        }
    }

    private fun convertToArguments(params: Params?): Array<Any?> {
        fun nulls() =
            object : Iterator<JsonNode?> {
                override fun hasNext(): Boolean = true
                override fun next(): JsonNode? = null
            }.asSequence()

        val jsonValues = when (params) {
            null -> nulls()
            is ByNameParams -> paramNames.asSequence().map { params.params[it] }
            is ByPositionParams -> params.params.asSequence() + nulls()
        }

        return jsonValues
            .zip(parameters.asSequence())
            .map { (node, param) ->
                node?.parseAs(param.type) ?: param.defaultValue.getOrNull
            }
            .toArray(parameters.size)
    }
}
