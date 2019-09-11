package com.hylamobile.voorhees.server.spring.webmvc

import com.fasterxml.jackson.databind.JsonNode
import com.hylamobile.voorhees.jsonrpc.*
import com.hylamobile.voorhees.server.annotation.optionDefaultValue
import com.hylamobile.voorhees.server.annotation.paramAnno
import com.hylamobile.voorhees.util.Option
import com.hylamobile.voorhees.util.toArray
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.web.HttpRequestHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Type
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface JsonRpcHandler : HttpRequestHandler

fun ErrorCode.toHandler(data: Any?) = JsonRpcErrorHandler(toError(data))

class JsonRpcErrorHandler(private val error: Error) : JsonRpcHandler {

    override fun handleRequest(httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {
        val jsonRequest = httpRequest.jsonRequest
        val jsonResponse = Response.error(error, jsonRequest.id)
        httpResponse.send(jsonResponse)
    }
}

class JsonRpcMethodHandler(private val bean: Any, private val method: Method) : JsonRpcHandler {

    companion object {
        val parameterNameDiscoverer = DefaultParameterNameDiscoverer()

        data class MethodParameter(
            val name: String,
            val type: Type,
            val defaultValue: Option<Any?> = Option.none())
    }

    private val paramByName: LinkedHashMap<String, MethodParameter> =
        method.parameters
            .zip(parameterNameDiscoverer.getParameterNames(method) ?: emptyArray())
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

    override fun handleRequest(httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {
        val jsonRequest = httpRequest.jsonRequest

        val args = convertToArguments(jsonRequest.params)

        try {
            val result = method.invoke(bean, *args)
            val jsonResponse = Response.success(result, jsonRequest.id)

            httpResponse.send(jsonResponse)
        }
        catch (ex: InvocationTargetException) {
            ex.printStackTrace()
            val targetEx = ex.targetException
            val error =
                if (targetEx is JsonRpcException) targetEx.error
                else ErrorCode.INTERNAL_ERROR.toError(targetEx.message)

            val jsonResponse = Response.error(error, jsonRequest.id)
            httpResponse.send(jsonResponse)
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
